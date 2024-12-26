package com.project.userservice.order.repository;


import com.project.userservice.order.entity.WishProduct;
import com.project.userservice.user.entity.User;

import java.util.List;

public interface WishProductDslRepository {
    List<WishProduct> findByWishProductByUser(User user);
}
