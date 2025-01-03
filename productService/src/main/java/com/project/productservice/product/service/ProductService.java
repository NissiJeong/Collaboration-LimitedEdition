package com.project.productservice.product.service;

import com.project.productservice.product.dto.ProductDto;
import com.project.productservice.product.entity.Product;
import com.project.productservice.product.repository.ProductRepository;
import com.project.productservice.product.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisRepository redisRepository;

    public ProductDto saveProduct(ProductDto requestDto) {
        Product product = new Product(requestDto.getProductName(), requestDto.getStock(), requestDto.getImageUrl(), requestDto.getDetailInfo(),requestDto.getPrice());

        // 상품 저장
        Product savedProduct = productRepository.save(product);

        // 상품 재고 Redis 에 저장
        String key = "product:"+savedProduct.getId()+":stock";
        int stock = savedProduct.getStock();
        redisRepository.saveData(key, String.valueOf(stock));

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

    @Transactional
    public ProductDto changeProductStockByOrder(Long productId, ProductDto productDto, String type) {
        Product product = productRepository.findById(productId).orElseThrow(()->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        product.changeStockByOrderQuantity(productDto.getOrderQuantity(), type);

        String key = "product:"+product.getId()+":stock";
        redisRepository.decrementData(key, productDto.getOrderQuantity());

        return ProductDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .stock(product.getStock())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .detailInfo(product.getDetailInfo()).build();
    }

    public List<ProductDto> getProductList(List<ProductDto> productDtoList) {
        List<Long> productIds = productDtoList.stream().map(ProductDto::getProductId).toList();

        List<Product> productList = productRepository.findAllById(productIds);

        return productList.stream().map(product ->
                ProductDto.builder()
                        .productId(product.getId())
                        .productName(product.getProductName())
                        .imageUrl(product.getImageUrl())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .detailInfo(product.getDetailInfo()).build()).toList();
    }

    public Integer getProductDetailStock(Long productId) {
        // redis 에서 수량 읽어오기(캐싱)
        String key = "product:"+productId+":stock";
        String stockString = redisRepository.getData(key);
        int stock = 0;

        // redis 에 수량 존재하지 않으면 MySQL 에서 읽어온 후 redis 에 다시 저장
        if(stockString == null) {
            Product product = productRepository.findById(productId).orElseThrow(()->
                    new NullPointerException("해당 상품이 존재하지 않습니다.")
            );

            stock = product.getStock();
            redisRepository.saveData(key, String.valueOf(product.getStock()));
        }

        return stock;
    }
}
