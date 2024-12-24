package com.project.collaboration.product.service;

import com.project.collaboration.product.dto.ProductDto;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.repository.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    ProductRepository productRepository;

    @Test
    @DisplayName("product 생성 테스트")
    void saveProductTest() {
        ProductDto productDto = ProductDto.builder()
                .productName("test")
                .stock(100)
                .price(1000000)
                .detailInfo("test detail info")
                .imageUrl("test image url").build();

        Product product = new Product(productDto.getProductName(), productDto.getStock(), productDto.getImageUrl(), productDto.getDetailInfo(), productDto.getPrice());
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductService productService = new ProductService(productRepository);
        ProductDto result = productService.saveProduct(productDto);

        assertThat(productDto.getProductName()).isEqualTo(result.getProductName());
    }

    @Test
    @DisplayName("product 조회 테스트")
    void getProductListTest() {
        List<Product> list = new ArrayList<>();
        list.add(new Product("test1", 10, null, null, 1000));
        list.add(new Product("test2", 10, null, null, 1000));

        when(productRepository.findAll()).thenReturn(list);
        ProductService productService = new ProductService(productRepository);
        List<ProductDto> resultList = productService.getProductList();

        assertThat(list.size()).isEqualTo(resultList.size());
    }

    @Test
    @DisplayName("product 상세 테스트")
    void getProductDetailTest() {
        Product product = new Product("test1", 10, null, null, 10000);

        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        ProductService productService = new ProductService(productRepository);
        ProductDto resultDto = productService.getProductDetail(1L);

        assertThat(product.getProductName()).isEqualTo(resultDto.getProductName());
    }

    @Test
    @DisplayName("product 상세 테스트: 상품이 존재하지 않는 경우")
    void getProductDetailTest1() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());
        ProductService productService = new ProductService(productRepository);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            productService.getProductDetail(1L);
        });

        assertEquals("해당 상품이 존재하지 않습니다.", exception.getMessage());
    }
}