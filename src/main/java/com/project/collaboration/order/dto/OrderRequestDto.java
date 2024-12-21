package com.project.collaboration.order.dto;

import com.project.collaboration.product.dto.ProductDto;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderRequestDto {
    private List<ProductDto> productDtoList;
    private AddressDto addressDto;
    private int orderQuantity;
}
