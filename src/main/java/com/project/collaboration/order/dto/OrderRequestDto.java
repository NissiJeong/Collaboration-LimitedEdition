package com.project.collaboration.order.dto;

import com.project.collaboration.product.dto.ProductDto;

import java.util.List;

public class OrderRequestDto {
    private List<ProductDto> productDtoList;
    private AddressDto addressDto;
}
