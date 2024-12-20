package com.project.collaboration.order.controller;

import com.project.collaboration.order.dto.WishProductDto;
import com.project.collaboration.order.service.WishProductService;
import com.project.collaboration.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/wish/product")
@RequiredArgsConstructor
public class WishProductController {

    private final WishProductService wishProductService;

    @PostMapping(value = "/{productId}")
    public ResponseEntity<?> registerWishProduct(@PathVariable Long productId,
                                                 @RequestBody WishProductDto requestDto,
                                                 @AuthenticationPrincipal UserDetailsImpl userDetails) {
        WishProductDto responseDto = wishProductService.saveWishProduct(productId, requestDto, userDetails);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getWishProductList(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<WishProductDto> responseDtoList = wishProductService.getWishProductList(userDetails);
        return ResponseEntity.ok(responseDtoList);
    }

    @PutMapping
    public ResponseEntity<?> updateWishProduct(@RequestBody WishProductDto requestDto) {
        WishProductDto responseDto = wishProductService.updateWishProduct(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteWishProduct(@RequestBody WishProductDto requestDto) {
        boolean isDelete = wishProductService.deleteWishProduct(requestDto);
        return ResponseEntity.ok(isDelete);
    }
}
