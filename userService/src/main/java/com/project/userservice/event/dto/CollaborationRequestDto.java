package com.project.userservice.event.dto;

import com.project.userservice.product.dto.ProductDto;
import lombok.Getter;

@Getter
public class CollaborationRequestDto {
    private ProductDto productDto;
    private BrandDto brandDto;
    private CollaborationDto collaborationDto;
}
