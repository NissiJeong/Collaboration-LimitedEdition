package com.project.collaboration.order.repository;

import com.project.collaboration.order.entity.WishProduct;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishProductRepository extends JpaRepository<WishProduct, Long>, WishProductDslRepository {
    WishProduct findByProductAndUser(Product product, User user);
}
