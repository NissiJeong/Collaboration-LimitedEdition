package com.project.userservice.order.repository;

import com.project.userservice.order.entity.WishProduct;
import com.project.userservice.product.entity.Product;
import com.project.userservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishProductRepository extends JpaRepository<WishProduct, Long>, WishProductDslRepository {
    WishProduct findByProductAndUser(Product product, User user);
}
