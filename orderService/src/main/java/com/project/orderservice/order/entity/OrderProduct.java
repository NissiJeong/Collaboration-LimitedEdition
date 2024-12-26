package com.project.orderservice.order.entity;

import com.project.orderservice.order.dto.ProductDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class OrderProduct extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Long productId;

    private String productName;

    private int orderPrice;

    private int orderQuantity;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String detailInfo;

    public OrderProduct(Order order, ProductDto productDto, int orderQuantity) {
        this.order = order;
        this.productId = productDto.getProductId();
        this.productName = productDto.getProductName();
        this.orderPrice = productDto.getPrice();
        this.orderQuantity = orderQuantity;
        this.imageUrl = productDto.getImageUrl();
        this.detailInfo = productDto.getDetailInfo();
    }
}
