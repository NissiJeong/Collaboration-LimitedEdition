package com.project.orderservice.event.service;

import com.project.orderservice.event.dto.BrandDto;
import com.project.orderservice.event.dto.CollaborationDto;
import com.project.orderservice.event.dto.CollaborationRequestDto;
import com.project.orderservice.event.entity.Brand;
import com.project.orderservice.event.entity.Collaboration;
import com.project.orderservice.event.repository.BrandRepository;
import com.project.orderservice.event.repository.CollaborationRepository;
import com.project.orderservice.product.dto.ProductDto;
import com.project.orderservice.product.entity.Product;
import com.project.orderservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollaborationService {

    private final CollaborationRepository collaborationRepository;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public CollaborationDto saveCollaborationProduct(CollaborationRequestDto requestDto) {
        ProductDto productDto = requestDto.getProductDto();
        BrandDto brandDto = requestDto.getBrandDto();
        CollaborationDto collaborationDto = requestDto.getCollaborationDto();

        // 프로덕트 없으면 생성
        Product product = null;
        if(productDto.getProductId() == null)
            product = productRepository.save(new Product(productDto.getProductName(),
                    productDto.getStock(),
                    productDto.getImageUrl(),
                    productDto.getDetailInfo(),
                    productDto.getPrice()));
        else
            product = productRepository.findById(productDto.getProductId()).orElseThrow(() ->
                    new NullPointerException("해당 상품이 존재하지 않습니다.")
            );


        // 브랜드 없으면 생성
        Brand brand = null;
        if(brandDto.getBrandId() == null)
            brand = brandRepository.save(new Brand(brandDto));
        else
            brand = brandRepository.findById(brandDto.getBrandId()).orElseThrow(() ->
                    new NullPointerException("해당 브랜드가 존재하지 않습니다.")
            );

        // 콜라보레이션 생성
        Collaboration collaboration = collaborationRepository.save(new Collaboration(product, brand, collaborationDto));

        return CollaborationDto.builder()
                .collaborationId(collaboration.getId())
                .detailInfo(collaboration.getDetailInfo())
                .eventName(collaboration.getEventName())
                .startDate(collaboration.getStartDate())
                .endDate(collaboration.getEndDate()).build();
    }
}
