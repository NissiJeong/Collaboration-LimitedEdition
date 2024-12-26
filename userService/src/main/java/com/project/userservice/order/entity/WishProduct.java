package com.project.userservice.order.entity;

import com.project.userservice.common.entity.Timestamped;
import com.project.userservice.order.dto.WishProductDto;
import com.project.userservice.product.entity.Product;
import com.project.userservice.user.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    public WishProduct(WishProductDto requestDto, Product product, User user) {
        this.wishQuantity = requestDto.getWishQuantity();
        this.product = product;
        this.user = user;
    }

    public void changeWishProduct(int wishQuantity) {
        this.wishQuantity = wishQuantity;
    }
}