package com.project.productservice.product.dto;

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
    private int price;
    private String detailInfo;
    private int orderQuantity;

    @Builder
    public ProductDto(Long productId, String productName,
                      int stock, String imageUrl,
                      int price,
                      String detailInfo,
                      int orderQuantity) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.price = price;
        this.detailInfo = detailInfo;
        this.orderQuantity = orderQuantity;
    }
}
