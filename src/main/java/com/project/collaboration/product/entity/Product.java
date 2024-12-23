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

    @Column(columnDefinition = "TEXT")
    private String detailInfo;

    private int price;

    public Product(String productName, int stock, String imageUrl, String detailInfo, int price) {
        this.productName = productName;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.detailInfo = detailInfo;
        this.price = price;
    }

    public void changeStockByOrderQuantity(int orderQuantity, String type) {
        if(type.equals("minus") && stock - orderQuantity >= 0) {
            stock -= orderQuantity;
        }
        else if(type.equals("plus")) {
            stock += orderQuantity;
        }
    }
}
