package com.project.orderservice.event.entity;

import com.project.orderservice.event.dto.CollaborationDto;
import com.project.orderservice.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
public class Collaboration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "collaboration_product_id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;


    private String detailInfo;
    private String eventName;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Collaboration(Product product, Brand brand, CollaborationDto collaborationDto) {
        this.product = product;
        this.brand = brand;
        this.detailInfo = collaborationDto.getDetailInfo();
        this.eventName = collaborationDto.getEventName();
        this.startDate = collaborationDto.getStartDate();
        this.endDate = collaborationDto.getEndDate();
    }
}
