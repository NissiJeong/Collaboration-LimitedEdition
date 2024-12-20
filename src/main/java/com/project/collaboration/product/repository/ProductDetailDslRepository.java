package com.project.collaboration.product.repository;

import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.entity.ProductDetail;

public interface ProductDetailDslRepository {
    ProductDetail findByProductAndVersion(Long productId, int version);
}
