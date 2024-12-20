package com.project.collaboration.product.service;

import com.project.collaboration.product.dto.ProductDetailDto;
import com.project.collaboration.product.dto.ProductDto;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.entity.ProductDetail;
import com.project.collaboration.product.repository.ProductDetailRepository;
import com.project.collaboration.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductDetailRepository productDetailRepository;

    public ProductDto saveProduct(ProductDto requestDto) {
        // 최초 버전 0으로 설정(detail 만들어지지 않아서)
        Product product = new Product(requestDto.getProductName(), requestDto.getStock(), requestDto.getImageUrl(), 0);

        // 상품 마스터 저장
        Product savedProduct = productRepository.save(product);

        return new ProductDto().builder()
                .productId(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .stock(savedProduct.getStock())
                .imageUrl(savedProduct.getImageUrl())
                .version(savedProduct.getVersion()).build();
    }

    public ProductDetailDto saveProductDetail(Long productId, ProductDetailDto requestDto) {
        Product product = productRepository.findById(productId).orElseThrow(()->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        product.increaseVersion(product.getVersion()+1);
        ProductDetail productDetail = new ProductDetail(requestDto, product);

        ProductDetail savedProductDetail = productDetailRepository.save(productDetail);

        return new ProductDetailDto().builder()
                .id(savedProductDetail.getId())
                .version(savedProductDetail.getVersion())
                .price(savedProductDetail.getPrice())
                .productDetailInfo(savedProductDetail.getProductDetailInfo()).build();
    }

    public List<ProductDto> getProductList() {
        List<Product> productList = productRepository.findAll();

        return productList.stream().map(product ->
                new ProductDto().builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .imageUrl(product.getImageUrl())
                        .stock(product.getStock())
                        .version(product.getVersion()).build()).toList();
    }

    public ProductDto getProductDeatil(Long productId, int version) {
        Product product = productRepository.findById(productId).orElseThrow(()->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        ProductDetail productDetail = productDetailRepository.findByProductAndVersion(product.getId(), version);
        if(productDetail == null) {
            throw new NullPointerException("해당 상품이 존재하지 않습니다.");
        }

        ProductDetailDto productDetailDto = new ProductDetailDto().builder()
                .id(productDetail.getId())
                .price(productDetail.getPrice())
                .productDetailInfo(productDetail.getProductDetailInfo())
                .version(productDetail.getVersion()).build();

        return new ProductDto().builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .version(product.getVersion())
                .productDetailDto(productDetailDto).build();
    }
}
