package com.project.collaboration.event.entity;

import com.project.collaboration.event.dto.BrandDto;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_id")
    private Long id;

    private String brandName;

    private String detailInfo;

    public Brand(BrandDto brandDto) {
        this.brandName = brandDto.getBrandName();
        this.detailInfo = brandDto.getDetailInfo();
    }
}
