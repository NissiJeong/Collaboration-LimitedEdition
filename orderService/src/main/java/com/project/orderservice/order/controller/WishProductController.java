package com.project.orderservice.order.controller;

import com.project.orderservice.order.dto.WishProductDto;
import com.project.orderservice.order.service.WishProductService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/wish/product")
@RequiredArgsConstructor
public class WishProductController {

    private final WishProductService wishProductService;

    @PostMapping(value = "/{productId}")
    public ResponseEntity<?> registerWishProduct(@PathVariable Long productId,
                                                 @RequestBody WishProductDto requestDto, HttpServletRequest request) {
        WishProductDto responseDto = wishProductService.saveWishProduct(productId, requestDto, request);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getWishProductList(HttpServletRequest request) {
        List<WishProductDto> responseDtoList = wishProductService.getWishProductList(request);
        return ResponseEntity.ok(responseDtoList);
    }

    @PutMapping(value = "/{wishProductId}")
    public ResponseEntity<?> updateWishProduct(@PathVariable Long wishProductId, @RequestBody WishProductDto requestDto) {
        WishProductDto responseDto = wishProductService.updateWishProduct(wishProductId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping(value = "/{wishProductId}")
    public ResponseEntity<?> deleteWishProduct(@PathVariable Long wishProductId) {
        boolean isDelete = wishProductService.deleteWishProduct(wishProductId);
        return ResponseEntity.ok(isDelete);
    }
}
