package com.project.orderservice.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequestDto {
    private List<OrderProductDto> orderProductDtoList;
    private Long addressId;
    private String requestType;
}
