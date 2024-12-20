package com.project.collaboration.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ProductDto {

    private Long productId;
    private String productName;
    private int stock;
    private String imageUrl;
    private ProductDetailDto productDetailDto;
    private int version;

    @Builder
    public ProductDto(Long productId, String productName, int stock, String imageUrl, ProductDetailDto productDetailDto, int version) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.productDetailDto = productDetailDto;
        this.version = version;
    }
}
