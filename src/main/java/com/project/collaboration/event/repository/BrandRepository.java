package com.project.collaboration.event.repository;

import com.project.collaboration.event.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
