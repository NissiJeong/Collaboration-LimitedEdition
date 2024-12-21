package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.product.entity.Product;
import jakarta.persistence.*;

@Entity
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
    private String detail_info;
}
