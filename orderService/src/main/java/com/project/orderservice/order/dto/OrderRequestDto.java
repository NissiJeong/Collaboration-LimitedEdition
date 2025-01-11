package com.project.orderservice.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class OrderRequestDto {
    private Long orderId;
    private Long userId;
    private Long paymentId;
    private List<OrderProductDto> orderProductDtoList;
    private Long addressId;
    private String requestType;
    private Long productId;
    private int orderQuantity;

    @Builder
    public OrderRequestDto(Long orderId,
                           Long userId,
                           Long paymentId,
                           List<OrderProductDto> orderProductDtoList,
                           Long addressId,
                           String requestType,
                           Long productId,
                           int orderQuantity) {
        this.orderId = orderId;
        this.userId = userId;
        this.paymentId = paymentId;
        this.orderProductDtoList = orderProductDtoList;
        this.addressId = addressId;
        this.requestType = requestType;
        this.productId = productId;
        this.orderQuantity = orderQuantity;
    }
}
