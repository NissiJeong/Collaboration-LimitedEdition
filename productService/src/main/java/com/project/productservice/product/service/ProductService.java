package com.project.productservice.product.service;

import com.project.productservice.product.dto.ProductDto;
import com.project.productservice.product.entity.Product;
import com.project.productservice.product.repository.ProductRepository;
import com.project.productservice.product.repository.RedisRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisRepository redisRepository;
    private final RedissonClient redissonClient;

    public ProductDto saveProduct(ProductDto requestDto) {
        Product product = new Product(requestDto);

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

    public List<ProductDto> getProductList(String eventYn) {
        List<Product> productList = null;

        if("N".equals(eventYn))
            productList = productRepository.findAll();
        else
            productList = productRepository.findAllByEventYn(eventYn);

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
        String lockKey = "lock:product:"+productId+":stock";
        RLock lock = redissonClient.getLock(lockKey);
        boolean available = false;

        try{
            available = lock.tryLock(10, 2, TimeUnit.SECONDS);

            if(!available) {
                throw new IllegalArgumentException("Lock 획득 실패");
            }

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

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            if(available)
                lock.unlock();
        }
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

    @Scheduled(cron = "0 16 15 * * *")
    @Transactional
    public void eventProductOpen() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);;
        List<Product> productList = productRepository.findAllByStartDateAndEventYn(now, "Y");
        if(productList != null && !productList.isEmpty()) {
            for(Product product : productList) {
                product.openEventProduct("Y");
            }
        }
    }
}
