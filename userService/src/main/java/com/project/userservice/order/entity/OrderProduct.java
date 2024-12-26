package com.project.userservice.order.entity;

import com.project.userservice.common.entity.Timestamped;
import com.project.userservice.product.entity.Product;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String productName;

    private int orderPrice;

    private int orderQuantity;

    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String detailInfo;

    public OrderProduct(Order order, Product product, int orderQuantity) {
        this.order = order;
        this.product = product;
        this.productName = product.getProductName();
        this.orderPrice = product.getPrice();
        this.orderQuantity = orderQuantity;
        this.imageUrl = product.getImageUrl();
        this.detailInfo = product.getDetailInfo();
    }
}
