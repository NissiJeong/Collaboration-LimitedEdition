package com.project.collaboration.order.entity;

import com.project.collaboration.common.entity.Timestamped;
import com.project.collaboration.user.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "Orders")
public class Order extends Timestamped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(value = EnumType.STRING)
    private OrderStatusEnum orderStatus;
}
