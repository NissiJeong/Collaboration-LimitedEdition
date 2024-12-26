package com.project.orderservice.event.dto;

import com.project.orderservice.product.dto.ProductDto;
import lombok.Getter;

@Getter
public class CollaborationRequestDto {
    private ProductDto productDto;
    private BrandDto brandDto;
    private CollaborationDto collaborationDto;
}
