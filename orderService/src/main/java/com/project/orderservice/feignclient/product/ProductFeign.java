package com.project.orderservice.feignclient.product;

import com.project.orderservice.order.dto.OrderProductDto;
import com.project.orderservice.order.dto.ProductDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "product-service")
public interface ProductFeign {

    @GetMapping(value = "/api/product/{productId}")
    Optional<ProductDto> getProduct(@PathVariable("productId") Long productId);

    @PutMapping(value = "/api/product/stock/{productId}")
    void changeProductStockByOrder(@PathVariable("productId") Long productId, @RequestBody OrderProductDto requestDto);

    @PutMapping(value ="/api/product/stock/cancel/{productId}")
    void plusProductStockByOrderCancel(@PathVariable("productId") Long productId, @RequestBody OrderProductDto requestDto);

    @PostMapping(value = "/api/product/bulk")
    List<ProductDto> getProductList(@RequestBody  List<ProductDto> productDtoList);

    @Retry(name = "productFeign", fallbackMethod = "fallbackResponse")
    @CircuitBreaker(name = "productFeign", fallbackMethod = "fallbackResponse")
    @GetMapping("/errorful/case1")
    String errorCase1();

    default String fallbackResponse(Exception ex) {
        return "Fallback response due to: " + ex.getMessage();
    }

    @GetMapping("/errorful/case2")
    String errorCase2();

    @GetMapping("/errorful/case3")
    String errorCase3();
}
