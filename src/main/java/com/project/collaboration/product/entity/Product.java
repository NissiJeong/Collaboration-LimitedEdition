package com.project.collaboration.product.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false)
    private String productName;

    private int stock;

    private String imageUrl;

    private int version;

    public Product(String productName, int stock, String imageUrl, int version) {
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.version = version;
    }

    public void increaseVersion(int version) {
        this.version = version;
    }
}
