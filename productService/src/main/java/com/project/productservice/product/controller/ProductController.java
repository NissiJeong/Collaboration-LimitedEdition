package com.project.productservice.product.controller;

import com.project.productservice.product.dto.ProductDto;
import com.project.productservice.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<?> registerProduct(@RequestBody ProductDto requestDto) {
        ProductDto responseDto = productService.saveProduct(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getProductList() {
        List<ProductDto> responseDtoList = productService.getProductList();
        return ResponseEntity.ok(responseDtoList);
    }

    @GetMapping(value = "/{productId}")
    public ResponseEntity<?> getProductDetail(@PathVariable Long productId) {
        ProductDto responseDto = productService.getProductDetail(productId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping(value = "/stock/{productId}")
    public ResponseEntity<?> changeProductStockByOrder(@PathVariable Long productId, @RequestBody ProductDto productDto) {
        ProductDto responseDto = productService.changeProductStockByOrder(productId, productDto);
        return ResponseEntity.ok(responseDto);
    }
}
