package com.project.collaboration.event.entity;

import com.project.collaboration.brand.entity.Brand;
import com.project.collaboration.product.entity.Product;
import jakarta.persistence.*;

@Entity
public class CollaborationProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_product_id")
    private Long id;

    private String collaborationDetail;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;
}
