package com.project.collaboration.order.repository;

import com.project.collaboration.order.entity.WishProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishProductRepository extends JpaRepository<WishProduct, Long> {
}
