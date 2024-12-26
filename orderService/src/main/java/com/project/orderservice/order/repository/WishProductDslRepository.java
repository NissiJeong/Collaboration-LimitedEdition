package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.WishProduct;

import java.util.List;

public interface WishProductDslRepository {
    List<WishProduct> findByWishProductByUserId(Long userId);
}
