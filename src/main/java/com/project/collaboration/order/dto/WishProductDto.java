package com.project.collaboration.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class WishProductDto {
    private Long wishProductId;
    private int wishQuantity;

    @Builder
    public WishProductDto(Long wishProductId, int wishQuantity) {
        this.wishProductId = wishProductId;
        this.wishQuantity = wishQuantity;
    }
}
