package com.project.collaboration.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ProductDetailDto {

    private Long id;
    private String productDetailInfo;
    private int price;
    private int version;

    @Builder
    public ProductDetailDto(Long id, String productDetailInfo, int price, int version) {
        this.id = id;
        this.productDetailInfo = productDetailInfo;
        this.price = price;
        this.version = version;
    }
}
