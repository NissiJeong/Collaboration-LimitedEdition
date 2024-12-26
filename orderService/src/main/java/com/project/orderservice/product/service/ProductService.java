package com.project.orderservice.product.service;

import com.project.orderservice.product.dto.ProductDto;
import com.project.orderservice.product.entity.Product;
import com.project.orderservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public ProductDto saveProduct(ProductDto requestDto) {
        Product product = new Product(requestDto.getProductName(), requestDto.getStock(), requestDto.getImageUrl(), requestDto.getDetailInfo(),requestDto.getPrice());

        // 상품 저장
        Product savedProduct = productRepository.save(product);

        return ProductDto.builder()
                .productId(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .stock(savedProduct.getStock())
                .imageUrl(savedProduct.getImageUrl())
                .detailInfo(savedProduct.getDetailInfo()).build();
    }

    public List<ProductDto> getProductList() {
        List<Product> productList = productRepository.findAll();

        return productList.stream().map(product ->
                ProductDto.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .imageUrl(product.getImageUrl())
                        .stock(product.getStock())
                        .detailInfo(product.getDetailInfo())
                        .price(product.getPrice()).build()).toList();
    }

    public ProductDto getProductDetail(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow(()->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        return ProductDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .detailInfo(product.getDetailInfo()).build();
    }
}
