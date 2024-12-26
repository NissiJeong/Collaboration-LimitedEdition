package com.project.userservice.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequestDto {
    private List<OrderProductDto> orderProductDtoList;
    private AddressDto addressDto;
    private String requestType;
}
