package com.project.productservice.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.repository.RedisRepository;
import com.project.productservice.product.dto.ProductDto;
import com.project.productservice.product.entity.Product;
import com.project.productservice.product.repository.ProductRepository;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {
    @Mock
    private ProductRepository productRepository;
    @Mock
    private RedisRepository redisRepository;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private ObjectMapper objectMapper;

    private RLock mockLock;

    @BeforeEach
    void setUp() {
        mockLock = mock(RLock.class);
        when(redissonClient.getFairLock(anyString())).thenReturn(mockLock);
    }

    @Test
    @DisplayName("상품 저장 테스트: 이벤트 상품")
    void saveProduct() throws JsonProcessingException {
        ProductDto requestDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("Y").build();
        Product product = new Product(requestDto);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        ProductDto result = productService.saveProduct(requestDto);

        assertThat(result.getProductName()).isEqualTo(requestDto.getProductName());
    }

    @Test
    @DisplayName("상품 저장 테스트: 일반 상품")
    void saveProduct2() throws JsonProcessingException {
        ProductDto requestDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").build();
        Product product = new Product(requestDto);

        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        ProductDto result = productService.saveProduct(requestDto);

        assertThat(result.getProductName()).isEqualTo(requestDto.getProductName());
    }

    @Test
    @DisplayName("상품 조회 테스트: 이벤트 구분자 없을 경우")
    void getProduct1() {
        String eventYn = null;
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            productService.getProductList(eventYn);
        });

        assertEquals("이벤트 여부 파라미터 값은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("상품 조회 테스트: 이벤트 구분자 빈 값일 경우")
    void getProduct2() {
        String eventYn = "";
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);

        Exception exception = assertThrows(NullPointerException.class, () -> {
            productService.getProductList(eventYn);
        });

        assertEquals("이벤트 여부 파라미터 값은 필수입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("상품 조회 테스트: 이벤트 상품")
    void getProduct3() {
        String eventYn = "Y";
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);

        ProductDto productDto1 = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("Y").build();
        ProductDto productDto2 = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("Y").build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto1);
        productDtoList.add(productDto2);

        List<Product> productList = new ArrayList<>();
        productList.add(new Product(productDto1));
        productList.add(new Product(productDto2));

        when(productRepository.findAllByEventYn("Y")).thenReturn(productList);

        List<ProductDto> resultList = productService.getProductList(eventYn);

        assertThat(resultList.size()).isEqualTo(productDtoList.size());
    }

    @Test
    @DisplayName("상품 조회 테스트: 일반 상품")
    void getProduct4() {
        String eventYn = "N";
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);

        ProductDto productDto1 = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").build();
        ProductDto productDto2 = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("Y").build();
        List<ProductDto> productDtoList = new ArrayList<>();
        productDtoList.add(productDto1);
        productDtoList.add(productDto2);

        List<Product> productList = new ArrayList<>();
        productList.add(new Product(productDto1));
        productList.add(new Product(productDto2));

        when(productRepository.findAll()).thenReturn(productList);

        List<ProductDto> resultList = productService.getProductList(eventYn);

        assertThat(resultList.size()).isEqualTo(productDtoList.size());
    }

    @Test
    @DisplayName("상품 상세 조회 테스트: 잘못된 키 값일 경우")
    void getProductDetail1() {
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        Exception exception = assertThrows(NullPointerException.class, () -> {
            productService.getProductDetail(1L);
        });

        assertEquals("해당 상품이 존재하지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("상품 상세 조회 테스트: Redis 수량이 Null 일 경우")
    void getProductDetail2() {
        ProductDto requestDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").build();
        Product product = new Product(requestDto);

        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(redisRepository.getData(anyString())).thenReturn(null);
        when(redisRepository.getData(anyString())).thenReturn("10000");

        ProductDto result = productService.getProductDetail(1L);

        assertThat(requestDto.getProductName()).isEqualTo(result.getProductName());
    }

    @Test
    @DisplayName("상품 상세 조회 테스트: Redis 수량이 ''일 경우")
    void getProductDetail3() {
        ProductDto requestDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").build();
        Product product = new Product(requestDto);

        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(redisRepository.getData(anyString())).thenReturn("");
        when(redisRepository.getData(anyString())).thenReturn("10000");

        ProductDto result = productService.getProductDetail(1L);

        assertThat(requestDto.getProductName()).isEqualTo(result.getProductName());
    }

    @Test
    void changeProductStockByOrder_success() throws InterruptedException {
        // Given
        Long productId = 1L;
        ProductDto requestDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").orderQuantity(10).build();
        Product product = new Product(productId, "testProduct", 10000, null, "testDetail", 10000, "Y", null, null, null);

        // RLock 획득 성공 시나리오
        when(mockLock.tryLock(10, 5, TimeUnit.SECONDS)).thenReturn(true);
        when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
        when(mockLock.isHeldByCurrentThread()).thenReturn(true);

        // When
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        ProductDto result = productService.changeProductStockByOrder(productId, requestDto, "plus");

        // Then
        assertNotNull(result);
        assertEquals(requestDto.getProductName(), result.getProductName());
        verify(redisRepository, times(1)).incrementData("product:" + productId + ":stock", 10);

        // Unlock 호출 검증
        verify(mockLock).unlock();
    }

    @Test
    void changeProductStockByOrder_lockAcquisitionFailed() throws InterruptedException {
        // Given
        Long productId = 1L;
        ProductDto productDto = ProductDto.builder().productName("testProduct").price(10000).stock(10000).detailInfo("testDetail").startDate(LocalDateTime.now()).eventYn("N").orderQuantity(10).build();

        // RLock 획득 실패 시나리오
        when(mockLock.tryLock(10, 5, TimeUnit.SECONDS)).thenReturn(false);

        // When & Then
        ProductService productService = new ProductService(productRepository, redisRepository, redissonClient, objectMapper);
        assertThrows(IllegalArgumentException.class, () ->
                productService.changeProductStockByOrder(productId, productDto, "plus")
        );

        // Unlock이 호출되지 않았는지 검증
        verify(mockLock, never()).unlock();
    }
}