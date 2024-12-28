package com.project.orderservice.feignclient.product;

import com.project.orderservice.order.dto.OrderProductDto;
import com.project.orderservice.order.dto.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@FeignClient(name = "product-service")
public interface ProductFeign {

    @GetMapping(value = "/api/product/{productId}")
    Optional<ProductDto> getProduct(@PathVariable("productId") Long productId);

    @PutMapping(value = "/api/product/stock/{productId}")
    void changeProductStockByOrder(@PathVariable("productId") Long productId, @RequestBody OrderProductDto requestDto);

    @PutMapping(value ="/api/product/stock/cancel/{productId}")
    void plusProductStockByOrderCancel(@PathVariable("productId") Long productId, @RequestBody OrderProductDto requestDto);
}
