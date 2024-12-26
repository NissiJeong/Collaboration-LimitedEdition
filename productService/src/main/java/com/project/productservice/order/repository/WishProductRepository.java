package com.project.productservice.order.repository;

import com.project.productservice.order.entity.WishProduct;
import com.project.productservice.product.entity.Product;
import com.project.productservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishProductRepository extends JpaRepository<WishProduct, Long>, WishProductDslRepository {
    WishProduct findByProductAndUser(Product product, User user);
}
