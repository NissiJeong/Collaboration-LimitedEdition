package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.WishProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishProductRepository extends JpaRepository<WishProduct, Long>, WishProductDslRepository {
    WishProduct findByProductIdAndUserId(Long productId, Long userId);
}
