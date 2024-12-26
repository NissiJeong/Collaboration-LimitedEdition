package com.project.orderservice.event.repository;

import com.project.orderservice.event.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
