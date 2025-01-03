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
    private List<OrderProductDto> orderProductDtoList;
    private Long addressId;
    private String requestType;

    @Builder
    public OrderRequestDto(Long orderId, Long userId, List<OrderProductDto> orderProductDtoList, Long addressId, String requestType) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderProductDtoList = orderProductDtoList;
        this.addressId = addressId;
        this.requestType = requestType;
    }
}
