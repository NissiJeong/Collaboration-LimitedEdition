package com.project.orderservice.order.service;

import com.esotericsoftware.kryo.util.ObjectMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.dto.KafkaMessage;
import com.project.common.repository.RedisRepository;
import com.project.orderservice.feignclient.product.ProductFeign;
import com.project.orderservice.order.dto.OrderProductDto;
import com.project.orderservice.order.dto.OrderRequestDto;
import com.project.orderservice.order.dto.OrderResponseDto;
import com.project.orderservice.order.dto.ProductDto;
import com.project.orderservice.order.entity.Order;
import com.project.orderservice.order.entity.OrderProduct;
import com.project.orderservice.order.entity.OrderStatusEnum;
import com.project.orderservice.order.repository.OrderProductRepository;
import com.project.orderservice.order.repository.OrderRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductFeign productFeign;
    private final OrderProducerService orderProducerService;
    private final RedisRepository redisRepository;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public OrderResponseDto saveOrder(OrderRequestDto orderRequestDto, HttpServletRequest request) throws JsonProcessingException {
        // X-Claim-sub 헤더 값을 가져오기
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));
        Long addressId = orderRequestDto.getAddressId();

        // 2. Order Product 등록
        // 2-1. Product id 로 Product 정보 가져오기
        // 2-2. Order Product 등록
        Long productId = orderRequestDto.getProductId();
        log.info("productId: {}",productId);
        int orderQuantity = orderRequestDto.getOrderQuantity();
        ProductDto productDto = checkAvailableOrder(productId, orderQuantity).orElseThrow(()->
                new IllegalArgumentException("주문 가능한 상태가 아닙니다.")
        );

        // 1. Order 등록(주문 진행 중)
        Order savedOrder = orderRepository.save(new Order(userId, addressId, OrderStatusEnum.IN_PROGRESS));

        // order product data 저장
        OrderProduct orderProduct = new OrderProduct(savedOrder, productDto, orderQuantity);
        orderProductRepository.save(orderProduct);

        // 주문 생성 이벤트 발생(비동기 처리) -> 결제 서비스: 결제 진입 생성
        orderProducerService.sendMessage(
                "order-topic",
                new KafkaMessage<>("order_create", OrderRequestDto.builder()
                        .orderId(savedOrder.getId())
                        .orderProductDtoList(orderRequestDto.getOrderProductDtoList()).build()),
                userId);

        return OrderResponseDto.builder().orderId(savedOrder.getId()).productId(productId).orderQuantity(orderQuantity).build();
    }

    public void saveReservation(OrderResponseDto orderResponseDto) {
        Long orderId = orderResponseDto.getOrderId();
        Long productId = orderResponseDto.getProductId();
        int orderQuantity = orderResponseDto.getOrderQuantity();

        // 예약 시에 상품별로 lock 획득하여서 동시성 이슈가 생기지 않도록
        String lockKey = "lock:reservation:product:"+productId+":stock";
        RLock fairLock = redissonClient.getFairLock(lockKey);
        boolean available = false;

        try{
            available = fairLock.tryLock(10, 3, TimeUnit.SECONDS);

            if(!available) {
                throw new IllegalArgumentException("Lock 획득 실패");
            }

            String key = "product:"+productId+":stock";
            // 예약으로 인한 재고 감소
            redisRepository.decrementData(key, orderQuantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            // 락이 이용가능한 상태이고 현재 스레드가 점유하고 있으면 unlock
            if(available && fairLock.isHeldByCurrentThread())
                fairLock.unlock();
        }

        // 예약 내역, 백업 내역 레디스에 저장
        List<String> reserveProduct = new ArrayList<>();
        reserveProduct.add("product:"+productId+":reserveQuantity:"+orderQuantity);
        String key = "reservation:order:"+orderId;
        redisRepository.saveListData(key, reserveProduct, 5L, TimeUnit.MINUTES);
        key = "backup:reservation:order:"+orderId;
        redisRepository.saveListData(key, reserveProduct, null, null);
    }

    private Optional<ProductDto> checkAvailableOrder(Long productId, int orderQuantity) throws JsonProcessingException {
        String key = "event:product:"+productId;
        String jsonValue = redisRepository.getData(key);
        if(jsonValue == null) {
            throw new NullPointerException("해당 상품이 존재하지 않습니다.");
        }
        ProductDto productDto = objectMapper.readValue(jsonValue, ProductDto.class);

        int stock = productDto.getStock();
        if(stock-orderQuantity < 0) {
            throw new IllegalArgumentException(productDto.getProductName()+" 상품은 "+stock+"개만 주문 가능합니다.");
        }

        return Optional.of(productDto);
    }

    // 매일 자정에 주문 상태값 업데이트
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateOrderStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // D+1: 오늘 날짜 -1 데이터 가져와서 update => 배송 중
        LocalDateTime d1Threshold = now.minusDays(1);
        int updatedToShipping = orderRepository.updateToShipping(d1Threshold);

        // D+2: 오늘 날짜 -2 데이터 가져와서 update => 배송 완료
        LocalDateTime d2Threshold = now.minusDays(2);
        int updatedToDelivered = orderRepository.updateToDelivered(d2Threshold);

        // 환불 중인 주문 d+1 되면 환불 완료로 처리 & product 재고 복구
        List<Order> orders = orderRepository.findAllByYesterdayAndInRefund(d1Threshold);
        for(Order order : orders) {
            // Order 안에 있는 Product 들 quantity 돌려놓기
            List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);

            // 주문 생성 이벤트 발생 -> 상품 서비스, 결제 서비스 각각 이벤트 처리
            orderProducerService.sendMessage(
                    "order-topic",
                    new KafkaMessage<>("order_cancel", OrderRequestDto.builder()
                            .orderId(order.getId())
                            .orderProductDtoList(
                                    orderProducts.stream().map(
                                            orderProduct -> OrderProductDto.builder()
                                                    .productId(orderProduct.getProductId())
                                                    .orderQuantity(orderProduct.getOrderQuantity()).build()).toList()).build()),
                    null);

            order.updateStats(OrderStatusEnum.REFUND_COMPLETE);
        }
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderRequestDto orderRequestDto, HttpServletRequest request) {
        // X-Claim-sub 헤더 값을 가져오기
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        // 사용자의 요청에 따른 메서드 실행
        String requestType = orderRequestDto.getRequestType();
        OrderResponseDto orderResponseDto = null;
        // 1. 주문 상품에 대한 취소(배송중이 되기 전까지만 취소 가능, 취소 후에는 재고 복구)
        if(requestType.equals("cancelOrder")) {
            orderResponseDto = cancelOrder(orderId, userId);
        }
        // 2. 상품에 대한 반품(배송 완료 후 D+1 까지만 반품 가능, 반품한 상품은 반품 신청 후 D+1에 재고 반영, 그 후 반품 완료로 변경)
        else if(requestType.equals("refundOrder")) {
            orderResponseDto = refundOrder(orderId, userId);
        }

        return orderResponseDto;
    }

    private OrderResponseDto refundOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() ->
                new IllegalArgumentException("해당 주문이 존재하지 않습니다.")
        );

        if (!order.canBeReturned()) {
            throw new IllegalStateException("반품 가능한 상품이 아닙니다.");
        }

        order.updateStats(OrderStatusEnum.IN_REFUND_DELIVERY);

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderStatus(order.getOrderStatus()).build();
    }

    private OrderResponseDto cancelOrder(Long orderId, Long userId) {
        // 배송아이디와 배송상태(배송 완료)로 조회
        Order order = orderRepository.findByIdAndOrderStatus(orderId, OrderStatusEnum.ORDER_COMPLETE).orElseThrow(()->
                new NullPointerException("해당 배송상품은 취소할 수 없습니다.")
        );

        if(!Objects.equals(order.getUserId(), userId)) {
            throw new IllegalArgumentException("본인이 아닌 경우 배송상태 변경이 불가능 합니다.");
        }

        // Order 안에 있는 Product 들 quantity 돌려놓기
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        // 주문 생성 이벤트 발생 -> 상품 서비스, 결제 서비스 각각 이벤트 처리
        orderProducerService.sendMessage(
                "order-topic",
                new KafkaMessage<>("order_cancel", OrderRequestDto.builder()
                        .orderId(orderId)
                        .orderProductDtoList(
                                orderProducts.stream().map(
                                    orderProduct -> OrderProductDto.builder()
                                            .productId(orderProduct.getProductId())
                                            .orderQuantity(orderProduct.getOrderQuantity()).build()).toList()).build()),
                userId);

        // Order 상태 변경
        order.updateStats(OrderStatusEnum.ORDER_CANCEL);

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderStatus(OrderStatusEnum.ORDER_CANCEL).build();
    }

    private long startTime;
    private boolean isRunning;

    //@Scheduled(fixedRate = 1000)
    public void errorCase1() {
        if (!isRunning) {
            startTime = System.currentTimeMillis(); // 실행 시작 시간 저장
            isRunning = true;
        }

        long elapsedTime = System.currentTimeMillis() - startTime;

        if (elapsedTime < 120000) { // 2분 동안만 실행 (120,000ms)
            for(int i=0; i<100; i++) {
                try {
                    String result = productFeign.errorCase1();
                    log.info("error case1: {}", result);
                } catch (Exception ex) {
                    log.error("Error calling errorCase1: {}", ex.getMessage());
                }
            }
        } else {
            // 2분이 지나면 스케줄러 중지
            isRunning = false;
            log.info("2 minutes have passed, stopping requests.");
        }
    }

    /**
     * 주문 완료 로직 처리
     * @param orderRequestDto
     */
    @Transactional
    public void completeOrder(OrderRequestDto orderRequestDto) {
        Order order = orderRepository.findById(orderRequestDto.getOrderId()).orElseThrow(() ->
                new IllegalArgumentException("해당 주문이 존재하지 않습니다.")
        );

        order.updateStats(OrderStatusEnum.ORDER_COMPLETE);
    }

    public void cancelOrder(OrderRequestDto orderRequestDto) {
        Order order = orderRepository.findById(orderRequestDto.getOrderId()).orElseThrow(() ->
                new IllegalArgumentException("해당 주문이 존재하지 않습니다.")
        );

        order.updateStats(OrderStatusEnum.ORDER_CANCEL);
    }
}
