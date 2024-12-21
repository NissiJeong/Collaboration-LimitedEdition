package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.order.dto.AddressDto;
import com.project.collaboration.user.entity.User;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Address extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String city;

    private String zipCode;

    private String firstAddress;

    private String secondAddress;

    private String defaultAddressYn;

    public Address(AddressDto addressDto, User user) {
        this.user = user;
        this.city = addressDto.getCity();
        this.zipCode = addressDto.getZipcode();
        this.firstAddress = addressDto.getFirstAddress();
        this.secondAddress = addressDto.getSecondAddress();
        this.defaultAddressYn = addressDto.getDefaultAddressYn();
    }
}
