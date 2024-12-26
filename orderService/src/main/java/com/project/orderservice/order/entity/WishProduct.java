package com.project.orderservice.order.entity;

import com.project.orderservice.order.dto.WishProductDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "wish_product")
@NoArgsConstructor
public class WishProduct extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wish_product_id")
    private Long id;

    private int wishQuantity;

    private Long userId;

    private Long productId;

    public WishProduct(WishProductDto requestDto, Long productId, Long userId) {
        this.wishQuantity = requestDto.getWishQuantity();
        this.productId = productId;
        this.userId = userId;
    }

    public void changeWishProduct(int wishQuantity) {
        this.wishQuantity = wishQuantity;
    }
}
