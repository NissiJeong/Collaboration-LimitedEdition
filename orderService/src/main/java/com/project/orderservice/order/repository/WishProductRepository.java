package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.WishProduct;
import com.project.orderservice.product.entity.Product;
import com.project.orderservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishProductRepository extends JpaRepository<WishProduct, Long>, WishProductDslRepository {
    WishProduct findByProductAndUser(Product product, User user);
}
