package com.project.collaboration.product.entity;

import com.project.collaboration.product.dto.ProductDetailDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_detail")
@NoArgsConstructor
@Getter
public class ProductDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_detail_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String productDetailInfo;

    private int price;

    // 변경 내역 관리..?
    private int version;

    public ProductDetail(ProductDetailDto requestDto, Product product) {
        this.productDetailInfo = requestDto.getProductDetailInfo();
        this.price = requestDto.getPrice();
        this.version = product.getVersion();
        this.product = product;
    }
}
