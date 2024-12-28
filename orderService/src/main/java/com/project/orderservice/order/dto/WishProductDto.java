package com.project.orderservice.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class WishProductDto {
    private Long wishProductId;
    private int wishQuantity;
    private ProductDto productDto;
    private List<ProductDto> productDtoList;

    @Builder
    public WishProductDto(Long wishProductId, int wishQuantity, ProductDto productDto, List<ProductDto> productDtoList) {
        this.wishProductId = wishProductId;
        this.wishQuantity = wishQuantity;
        this.productDto = productDto;
        this.productDtoList = productDtoList;
    }
}
