package com.project.productservice.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderProductDto {
    private Long productId;
    private int orderQuantity;

    @Builder
    public OrderProductDto(Long productId, int orderQuantity) {
        this.productId = productId;
        this.orderQuantity = orderQuantity;
    }
}
