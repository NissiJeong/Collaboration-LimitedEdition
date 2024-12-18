package com.project.collaboration.brand.entity;

import jakarta.persistence.*;

@Entity
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;

    private String brandName;

    private String brandDetail;
}
