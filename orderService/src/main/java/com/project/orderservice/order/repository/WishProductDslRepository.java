package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.WishProduct;
import com.project.orderservice.user.entity.User;

import java.util.List;

public interface WishProductDslRepository {
    List<WishProduct> findByWishProductByUser(User user);
}
