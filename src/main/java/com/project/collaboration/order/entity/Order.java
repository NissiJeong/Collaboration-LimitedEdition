package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.user.entity.User;
import jakarta.persistence.*;

@Entity
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    private int orderQuantity;

    private String deliveryStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;
}
