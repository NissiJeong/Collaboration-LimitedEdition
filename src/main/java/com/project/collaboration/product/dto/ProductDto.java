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
    private int version;
    private Long productDetailId;
    private int price;
    private String productDetailInfo;

    @Builder
    public ProductDto(Long productId, String productName,
                      int stock, String imageUrl,
                      int version,
                      Long productDetailId,
                      int price,
                      String productDetailInfo) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.version = version;
        this.productDetailId = productDetailId;
        this.price = price;
        this.productDetailInfo = productDetailInfo;
    }
}
