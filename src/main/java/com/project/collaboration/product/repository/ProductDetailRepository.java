package com.project.collaboration.product.repository;

import com.project.collaboration.product.entity.ProductDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductDetailRepository extends JpaRepository<ProductDetail, Long>, ProductDetailDslRepository {
}
