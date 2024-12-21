package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.user.entity.User;
import jakarta.persistence.*;

@Entity
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
}
