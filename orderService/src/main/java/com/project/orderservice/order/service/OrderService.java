package com.project.orderservice.order.service;

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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public OrderResponseDto saveOrder(OrderRequestDto orderRequestDto, HttpServletRequest request) {
//        AddressDto addressDto = orderRequestDto.getAddressDto();
//        String city = encryptService.encryptInfo(addressDto.getCity() == null ? "" : addressDto.getCity());
//        String zipCode = encryptService.encryptInfo(addressDto.getZipcode() == null ? "" : addressDto.getZipcode());
//        String firstAddress = encryptService.encryptInfo(addressDto.getFirstAddress() == null ? "" : addressDto.getFirstAddress());
//        String secondAddress = encryptService.encryptInfo(addressDto.getSecondAddress() == null ? "" : addressDto.getSecondAddress());
//
//        Address address = null;
//        // 주소가 존재하지 않으면 주소 저장
//        if(addressDto.getAddressId() == null) {
//            address = addressRepository.save(new Address(city, zipCode, firstAddress, secondAddress, addressDto.getDefaultAddressYn(),userDetails.getUser()));
//        } else {
//            address = addressRepository.findById(addressDto.getAddressId()).orElseThrow(() ->
//                    new NullPointerException("해당 주소가 존재하지 않습니다.")
//            );
//        }
        // X-Claim-email 헤더 값을 가져오기
        String email = request.getHeader("X-Claim-email");
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));
        String role = request.getHeader("X-Claim-auth");

        System.out.println("userId = " + userId);
        System.out.println("email = " + email);
        System.out.println("role = " + role);

        Long addressId = 1L;

        // 1. Order 등록
        Order savedOrder = orderRepository.save(new Order(userId, addressId));

        // 2. Order Product 등록
        // 2-1. Product id 로 Product 정보 가져오기
        // 2-2. Order Product 등록
        List<OrderProductDto> productDtoList = orderRequestDto.getOrderProductDtoList();
        List<OrderProduct> orderProducts = new ArrayList<>();
        for(OrderProductDto orderProductDto : productDtoList) {
//            Product product = productRepository.findById(orderProductDto.getProductId()).orElseThrow(() ->
//                    new NullPointerException("해당 상품이 존재하지 않습니다.")
//            );
            // TODO 각 주문에 대한 상품 정보 가져오기
            ProductDto productDto = ProductDto.builder().productName("test").build();

            // TODO 각각의 상품에 대한 재고 관리 - kafka
            //product.changeStockByOrderQuantity(orderProductDto.getOrderQuantity(), "minus");

            OrderProduct orderProduct = new OrderProduct(savedOrder, productDto, orderProductDto.getOrderQuantity());
            orderProducts.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProducts);

        return OrderResponseDto.builder()
                .orderId(savedOrder.getId())
                .orderStatus(savedOrder.getOrderStatus()).build();
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
            for(OrderProduct orderProduct : orderProducts) {
                Long productId = orderProduct.getProductId();
                // TODO 각각의 상품에 대한 재고 관리 - kafka
                //product.changeStockByOrderQuantity(orderProduct.getOrderQuantity(), "plus");
            }

            order.updateStats(OrderStatusEnum.REFUND_COMPLETE);
        }
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderRequestDto orderRequestDto, HttpServletRequest request) {
        Long userId = 1L;

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

        if(order.getUserId() != userId) {
            throw new IllegalArgumentException("본인이 아닌 경우 배송상태 변경이 불가능 합니다.");
        }

        // Order 안에 있는 Product 들 quantity 돌려놓기
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        for(OrderProduct orderProduct : orderProducts) {
            Long productId = orderProduct.getProductId();
            // TODO 각각의 상품에 대한 재고 관리 - kafka
            //product.changeStockByOrderQuantity(orderProduct.getOrderQuantity(), "plus");
        }

        // Order 상태 변경
        order.updateStats(OrderStatusEnum.ORDER_CANCEL);

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderStatus(OrderStatusEnum.ORDER_CANCEL).build();
    }
}
