package com.project.userservice.address.repository;

import com.project.userservice.address.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByIdAndUserId(Long addressId, long userId);

    List<Address> findAllByUserId(long userId);
}
