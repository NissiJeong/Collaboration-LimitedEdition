package com.project.collaboration.event.dto;

import com.project.collaboration.product.dto.ProductDto;
import lombok.Getter;

@Getter
public class CollaborationRequestDto {
    private ProductDto productDto;
    private BrandDto brandDto;
    private CollaborationDto collaborationDto;
}
