package com.project.collaboration.product.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "product_detail")
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    public Long id;

    @OneToOne
    public Product product;

    public String productDetail;
}
