package com.project.productservice.product.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.common.repository.RedisRepository;
import com.project.productservice.product.dto.PaymentOrderDto;
import com.project.productservice.product.dto.ProductDto;
import com.project.productservice.product.entity.Product;
import com.project.productservice.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.redisson.api.RedissonClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final RedisRepository redisRepository;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public ProductDto saveProduct(ProductDto requestDto) throws JsonProcessingException {
        Product product = new Product(requestDto);

        // 상품 저장
        Product savedProduct = productRepository.save(product);

        // 상품 재고 Redis 에 저장
        String key = "product:"+savedProduct.getId()+":stock";
        int stock = savedProduct.getStock();
        redisRepository.saveData(key, String.valueOf(stock));

        // 이벤트 상품인 경우 Redis 에 저장
        if(savedProduct.getEventYn().equals("Y")) {
            log.info("event product redis save: {}",savedProduct.getProductName());
            key = "event:product:"+savedProduct.getId();
            String jsonValue = objectMapper.writeValueAsString(savedProduct);
            redisRepository.saveData(key, jsonValue);
        }

        return ProductDto.builder()
                .productId(savedProduct.getId())
                .productName(savedProduct.getProductName())
                .stock(savedProduct.getStock())
                .imageUrl(savedProduct.getImageUrl())
                .detailInfo(savedProduct.getDetailInfo()).build();
    }

    public List<ProductDto> getProductList(String eventYn) {
        List<Product> productList = null;

        if(eventYn == null || eventYn.isEmpty()) {
            throw new NullPointerException("이벤트 여부 파라미터 값은 필수입니다.");
        }

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

        String key = "product:"+product.getId()+":stock";
        String stockStr = redisRepository.getData(key);
        if(stockStr == null || stockStr.isEmpty()) {
            redisRepository.saveData(key, String.valueOf(product.getStock()));
        }

        int stock = Integer.parseInt(redisRepository.getData(key));

        return ProductDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .stock(stock)
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .detailInfo(product.getDetailInfo()).build();
    }

    @Transactional
    public ProductDto changeProductStockByOrder(Long productId, ProductDto productDto, String type) {
        String lockKey = "lock:product:"+productId+":stock";
        // 락 획득 순서를 공평하게 보장하는 fairLock 사용, 먼저 락을 요청한 쓰레드가 먼저 락 획득.
        RLock fairLock = redissonClient.getFairLock(lockKey);
        boolean available = false;

        try{
            available = fairLock.tryLock(10, 5, TimeUnit.SECONDS);

            if(!available) {
                throw new IllegalArgumentException("Lock 획득 실패");
            }

            Product product = productRepository.findById(productId).orElseThrow(()->
                    new NullPointerException("해당 상품이 존재하지 않습니다.")
            );

            product.changeStockByOrderQuantity(productDto.getOrderQuantity(), type);

            String key = "product:"+product.getId()+":stock";
            if("plus".equals(type))
                redisRepository.incrementData(key, productDto.getOrderQuantity());

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
            // 락이 이용가능한 상태이고 현재 스레드가 점유하고 있으면 unlock
            if(available && fairLock.isHeldByCurrentThread())
                fairLock.unlock();
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
        else
            stock = Integer.parseInt(stockString);

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

    @Transactional
    public void changeProductStockByPayment(PaymentOrderDto paymentOrderDto, String type) {
        Long orderId = paymentOrderDto.getOrderId();
        String key = "reservation:order:"+orderId;
        List<String> productList = redisRepository.getEntireList(key);

        if(productList == null || productList.isEmpty())
            throw new IllegalArgumentException("해당 예약 내역이 없습니다.");

        for(String productKey : productList) {
            Long productId = Long.parseLong(productKey.split(":")[1]);
            int orderQuantity = Integer.parseInt(productKey.split(":")[3]);

            // Redis 에서 주문 마이너스 한 수량 읽어와서 MySQL 에 재고 마이너스
            String lockKey = "lock:product:"+productId+":stock";
            // 락 획득 순서를 공평하게 보장하는 fairLock 사용, 먼저 락을 요청한 쓰레드가 먼저 락 획득.
            RLock fairLock = redissonClient.getFairLock(lockKey);
            boolean available = false;

            try{
                available = fairLock.tryLock(10, 5, TimeUnit.SECONDS);

                if(!available) {
                    throw new IllegalArgumentException("Lock 획득 실패");
                }

                Product product = productRepository.findById(productId).orElseThrow(()->
                        new NullPointerException("해당 상품이 존재하지 않습니다.")
                );

                product.changeStockByOrderQuantity(orderQuantity, type);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                // 락이 이용가능한 상태이고 현재 스레드가 점유하고 있으면 unlock
                if(available && fairLock.isHeldByCurrentThread())
                    fairLock.unlock();
            }
        }

        redisRepository.deleteData(key);
    }

    @Transactional
    public void restockProductStockByPaymentCancel(PaymentOrderDto paymentOrderDto) {
        Long orderId = paymentOrderDto.getOrderId();
        String key = "reservation:order:"+orderId;
        List<String> productList = redisRepository.getEntireList(key);

        restockProductStock(productList);

        redisRepository.deleteData(key);
        redisRepository.deleteData("backup:"+key);
    }

    @Transactional
    public void restockProductStock(List<String> productList) {
        if(productList == null || productList.isEmpty())
            throw new IllegalArgumentException("해당 예약 내역이 없습니다.");

        for(String productKey : productList) {
            Long productId = Long.parseLong(productKey.split(":")[1]);
            int orderQuantity = Integer.parseInt(productKey.split(":")[3]);

            // redis 에서 예약으로 인해 decrement 한 수량 복구
            String lockKey = "lock:reservation:product:"+productId+":stock";
            RLock fairLock = redissonClient.getFairLock(lockKey);
            boolean available = false;

            try{
                available = fairLock.tryLock(10, 5, TimeUnit.SECONDS);

                if(!available) {
                    throw new IllegalArgumentException("Lock 획득 실패");
                }

                String key = "product:"+productId+":stock";
                // 결제 취소로 인한 예약 재고 복구
                redisRepository.incrementData(key, orderQuantity);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                // 락이 이용가능한 상태이고 현재 스레드가 점유하고 있으면 unlock
                if(available && fairLock.isHeldByCurrentThread())
                    fairLock.unlock();
            }
        }
    }
}
