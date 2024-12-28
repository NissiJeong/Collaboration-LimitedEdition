package com.project.userservice.address.entity;

import com.project.userservice.address.dto.AddressDto;
import com.project.userservice.user.entity.Timestamped;
import com.project.userservice.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class Address extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    private Long userId;

    private String city;

    private String zipCode;

    private String firstAddress;

    private String secondAddress;

    private String defaultAddressYn;

    public Address(AddressDto addressDto, long userId) {
        this.userId = userId;
        this.city = addressDto.getCity();
        this.zipCode = addressDto.getZipcode();
        this.firstAddress = addressDto.getFirstAddress();
        this.secondAddress = addressDto.getSecondAddress();
        this.defaultAddressYn = addressDto.getDefaultAddressYn();
    }

    public void updateAddress(AddressDto addressDto) {
        this.city = addressDto.getCity();
        this.zipCode = addressDto.getZipcode();
        this.firstAddress = addressDto.getFirstAddress();
        this.secondAddress = addressDto.getSecondAddress();
        this.defaultAddressYn = addressDto.getDefaultAddressYn();
    }
}
