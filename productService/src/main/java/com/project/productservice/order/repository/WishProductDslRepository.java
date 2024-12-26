package com.project.productservice.order.repository;


import com.project.productservice.order.entity.WishProduct;
import com.project.productservice.user.entity.User;

import java.util.List;

public interface WishProductDslRepository {
    List<WishProduct> findByWishProductByUser(User user);
}
