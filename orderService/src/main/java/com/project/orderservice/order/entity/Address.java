package com.project.orderservice.order.entity;

import com.project.orderservice.common.entity.Timestamped;
import com.project.orderservice.user.entity.User;
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

    public Address(String city, String zipCode, String firstAddress, String secondAddress, String defaultAddressYn, User user) {
        this.user = user;
        this.city = city;
        this.zipCode = zipCode;
        this.firstAddress = firstAddress;
        this.secondAddress = secondAddress;
        this.defaultAddressYn = defaultAddressYn;
    }
}
