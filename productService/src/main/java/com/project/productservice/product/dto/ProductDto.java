package com.project.productservice.product.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private String eventYn;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime startDate;
    @JsonFormat(pattern = "yyyyMMddHHmmss")
    private LocalDateTime endDate;

    @Builder
    public ProductDto(Long productId, String productName,
                      int stock, String imageUrl,
                      int price,
                      String detailInfo,
                      int orderQuantity,
                      String eventYn,
                      LocalDateTime startDate,
                      LocalDateTime endDate) {
        this.productId = productId;
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.price = price;
        this.detailInfo = detailInfo;
        this.orderQuantity = orderQuantity;
        this.eventYn = eventYn;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
