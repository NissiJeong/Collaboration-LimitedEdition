package com.project.collaboration.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AddressDto {
    private Long addressId;
    private String city;
    private String zipcode;
    private String firstAddress;
    private String secondAddress;
    private String defaultAddressYn;

    @Builder
    public AddressDto(String secondAddress, Long addressId, String city, String zipcode, String firstAddress, String defaultAddressYn) {
        this.secondAddress = secondAddress;
        this.addressId = addressId;
        this.city = city;
        this.zipcode = zipcode;
        this.firstAddress = firstAddress;
        this.defaultAddressYn = defaultAddressYn;
    }
}
