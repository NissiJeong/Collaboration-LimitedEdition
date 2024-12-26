package com.project.productservice.order.repository;

import com.project.productservice.order.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
