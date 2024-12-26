package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
