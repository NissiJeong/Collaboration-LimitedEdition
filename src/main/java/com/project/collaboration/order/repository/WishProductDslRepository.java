package com.project.collaboration.order.repository;

import com.project.collaboration.order.entity.WishProduct;
import com.project.collaboration.user.entity.User;

import java.util.List;

public interface WishProductDslRepository {
    List<WishProduct> findByWishProductByUser(User user);
}
