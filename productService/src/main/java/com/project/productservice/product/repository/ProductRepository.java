package com.project.productservice.product.repository;

import com.project.productservice.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByEventYn(String eventYn);

    List<Product> findAllByStartDateAndEventYn(LocalDateTime now, String eventYn);
}
