package com.project.collaboration.order.service;

import com.project.collaboration.order.dto.AddressDto;
import com.project.collaboration.order.dto.OrderProductDto;
import com.project.collaboration.order.dto.OrderRequestDto;
import com.project.collaboration.order.dto.OrderResponseDto;
import com.project.collaboration.order.entity.Address;
import com.project.collaboration.order.entity.Order;
import com.project.collaboration.order.entity.OrderProduct;
import com.project.collaboration.order.entity.OrderStatusEnum;
import com.project.collaboration.order.repository.AddressRepository;
import com.project.collaboration.order.repository.OrderProductRepository;
import com.project.collaboration.order.repository.OrderRepository;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.repository.ProductRepository;
import com.project.collaboration.security.UserDetailsImpl;
import com.project.collaboration.user.service.EncryptService;
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
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;
    private final EncryptService encryptService;

    @Transactional
    public OrderResponseDto saveOrder(OrderRequestDto orderRequestDto, UserDetailsImpl userDetails) {
        AddressDto addressDto = orderRequestDto.getAddressDto();
        String city = encryptService.encryptInfo(addressDto.getCity() == null ? "" : addressDto.getCity());
        String zipCode = encryptService.encryptInfo(addressDto.getZipcode() == null ? "" : addressDto.getZipcode());
        String firstAddress = encryptService.encryptInfo(addressDto.getFirstAddress() == null ? "" : addressDto.getFirstAddress());
        String secondAddress = encryptService.encryptInfo(addressDto.getSecondAddress() == null ? "" : addressDto.getSecondAddress());

        Address address = null;
        // 주소가 존재하지 않으면 주소 저장
        if(addressDto.getAddressId() == null) {
            address = addressRepository.save(new Address(city, zipCode, firstAddress, secondAddress, addressDto.getDefaultAddressYn(),userDetails.getUser()));
        } else {
            address = addressRepository.findById(addressDto.getAddressId()).orElseThrow(() ->
                    new NullPointerException("해당 주소가 존재하지 않습니다.")
            );
        }

        // 1. Order 등록
        Order savedOrder = orderRepository.save(new Order(userDetails.getUser(), address));

        // 2. Order Product 등록
        // 2-1. Product id 로 Product 정보 가져오기
        // 2-2. Order Product 등록
        List<OrderProductDto> productDtoList = orderRequestDto.getOrderProductDtoList();
        List<OrderProduct> orderProducts = new ArrayList<>();
        for(OrderProductDto orderProductDto : productDtoList) {
            Product product = productRepository.findById(orderProductDto.getProductId()).orElseThrow(() ->
                    new NullPointerException("해당 상품이 존재하지 않습니다.")
            );

            // 각각의 상품에 대한 재고 관리
            product.changeStockByOrderQuantity(orderProductDto.getOrderQuantity(), "minus");

            OrderProduct orderProduct = new OrderProduct(savedOrder, product, orderProductDto.getOrderQuantity());
            orderProducts.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProducts);

        return OrderResponseDto.builder()
                .orderId(savedOrder.getId())
                .orderStatus(savedOrder.getOrderStatus()).build();
    }

    // 매일 자정에 주문 상태값 업데이트
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateOrderStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // D+1: 오늘 날짜 -1 데이터 가져와서 update
        LocalDateTime d1Threshold = now.minusDays(1);
        int updatedToShipping = orderRepository.updateToShipping(d1Threshold);

        // D+2: 오늘 날짜 -2 데이터 가져와서 UPDAte
        LocalDateTime d2Threshold = now.minusDays(2);
        int updatedToDelivered = orderRepository.updateToDelivered(d2Threshold);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(Long orderId, OrderRequestDto orderRequestDto, UserDetailsImpl userDetails) {
        // 사용자의 요청에 따른 메서드 실행
        String requestType = orderRequestDto.getRequestType();
        OrderResponseDto orderResponseDto = null;
        // 1. 주문 상품에 대한 취소(배송중이 되기 전까지만 취소 가능, 취소 후에는 재고 복구)
        if(requestType.equals("cancelOrder")) {
            orderResponseDto = cancelOrder(orderId, userDetails);
        }
        // 2. 상품에 대한 반품(배송 완료 후 D+1 까지만 반품 가능, 반품한 상품은 반품 신청 후 D+1에 재고 반영, 그 후 반품 완료로 변경)

        return orderResponseDto;
    }

    private OrderResponseDto cancelOrder(Long orderId, UserDetailsImpl userDetails) {
        // 배송아이디와 배송상태(배송 완료)로 조회
        Order order = orderRepository.findByIdAndOrderStatus(orderId, OrderStatusEnum.ORDER_COMPLETE).orElseThrow(()->
                new NullPointerException("해당 배송상품은 취소할 수 없습니다.")
        );

        if(order.getUser().getId() != userDetails.getUser().getId()) {
            throw new IllegalArgumentException("본인이 아닌 경우 배송상태 변경이 불가능 합니다.");
        }

        // Order 안에 있는 Product 들 quantity 돌려놓기
        List<OrderProduct> orderProducts = orderProductRepository.findAllByOrder(order);
        for(OrderProduct orderProduct : orderProducts) {
            Product product = orderProduct.getProduct();
            product.changeStockByOrderQuantity(orderProduct.getOrderQuantity(), "plus");
        }

        // Order 상태 변경
        order.updateStats(OrderStatusEnum.ORDER_CANCEL);

        return OrderResponseDto.builder()
                .orderId(order.getId())
                .orderStatus(OrderStatusEnum.ORDER_CANCEL).build();
    }
}
