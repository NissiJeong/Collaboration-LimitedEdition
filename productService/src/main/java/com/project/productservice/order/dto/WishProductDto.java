package com.project.productservice.order.dto;

import com.project.productservice.product.dto.ProductDto;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WishProductDto {
    private Long wishProductId;
    private int wishQuantity;
    private ProductDto productDto;

    @Builder
    public WishProductDto(Long wishProductId, int wishQuantity, ProductDto productDto) {
        this.wishProductId = wishProductId;
        this.wishQuantity = wishQuantity;
        this.productDto = productDto;
    }
}
