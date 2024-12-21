package com.project.collaboration.order.service;

import com.project.collaboration.order.dto.AddressDto;
import com.project.collaboration.order.dto.OrderRequestDto;
import com.project.collaboration.order.dto.OrderResponseDto;
import com.project.collaboration.order.entity.Address;
import com.project.collaboration.order.entity.Order;
import com.project.collaboration.order.entity.OrderProduct;
import com.project.collaboration.order.repository.AddressRepository;
import com.project.collaboration.order.repository.OrderProductRepository;
import com.project.collaboration.order.repository.OrderRepository;
import com.project.collaboration.product.dto.ProductDto;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.repository.ProductRepository;
import com.project.collaboration.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final OrderProductRepository orderProductRepository;

    @Transactional
    public OrderResponseDto saveOrder(OrderRequestDto orderRequestDto, UserDetailsImpl userDetails) {
        AddressDto addressDto = orderRequestDto.getAddressDto();
        Address address = null;
        // 주소가 존재하지 않으면 주소 저장
        if(addressDto.getAddressId() == null) {
            address = addressRepository.save(new Address(addressDto, userDetails.getUser()));
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
        List<ProductDto> productDtoList = orderRequestDto.getProductDtoList();
        List<OrderProduct> orderProducts = new ArrayList<>();
        for(ProductDto productDto : productDtoList) {
            Product product = productRepository.findById(productDto.getProductId()).orElseThrow(() ->
                    new NullPointerException("해당 상품이 존재하지 않습니다.")
            );

            OrderProduct orderProduct = new OrderProduct(savedOrder, product, orderRequestDto.getOrderQuantity());
            orderProducts.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProducts);

        return OrderResponseDto.builder()
                .orderId(savedOrder.getId())
                .orderStatus(savedOrder.getOrderStatus()).build();
    }
}
