package com.project.productservice.event.dto;

import com.project.productservice.product.dto.ProductDto;
import lombok.Getter;

@Getter
public class CollaborationRequestDto {
    private ProductDto productDto;
    private BrandDto brandDto;
    private CollaborationDto collaborationDto;
}
