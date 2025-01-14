package com.project.productservice.product.entity;

import com.project.productservice.product.dto.ProductDto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(length = 1)
    private String eventYn;

    @Column(length = 1)
    private String openYn;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    public Product(ProductDto productDto) {
        this.productName = productDto.getProductName();
        this.stock = productDto.getStock();
        this.imageUrl = productDto.getImageUrl();
        this.detailInfo = productDto.getDetailInfo();
        this.price = productDto.getPrice();
        this.eventYn = productDto.getEventYn();
        this.startDate = productDto.getStartDate();
        this.endDate = productDto.getEndDate();
        this.openYn = "N";
    }

    public void changeStockByOrderQuantity(int orderQuantity, String type) {
        if(type.equals("minus") && stock - orderQuantity >= 0) {
            stock -= orderQuantity;
        }
        else if(type.equals("plus")) {
            stock += orderQuantity;
        }
    }

    public void openEventProduct(String openYn) {
        this.openYn = openYn;
    }
}
