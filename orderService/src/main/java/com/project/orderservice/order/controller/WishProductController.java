package com.project.orderservice.order.controller;

import com.project.common.dto.ResponseMessage;
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
    public ResponseEntity<ResponseMessage> registerWishProduct(@PathVariable Long productId,
                                                 @RequestBody WishProductDto requestDto, HttpServletRequest request) {
        WishProductDto responseDto = wishProductService.saveWishProduct(productId, requestDto, request);

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDto)
                .statusCode(200)
                .resultMessage("관심 상품 등록 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @GetMapping
    public ResponseEntity<ResponseMessage> getWishProductList(HttpServletRequest request) {
        List<WishProductDto> responseDtoList = wishProductService.getWishProductList(request);

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDtoList)
                .statusCode(200)
                .resultMessage("관심 상품 목록 조회 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @PutMapping(value = "/{wishProductId}")
    public ResponseEntity<ResponseMessage> updateWishProduct(@PathVariable Long wishProductId, @RequestBody WishProductDto requestDto) {
        WishProductDto responseDto = wishProductService.updateWishProduct(wishProductId, requestDto);

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDto)
                .statusCode(200)
                .resultMessage("관심 상품 수정 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @DeleteMapping(value = "/{wishProductId}")
    public ResponseEntity<ResponseMessage> deleteWishProduct(@PathVariable Long wishProductId) {
        boolean isDelete = wishProductService.deleteWishProduct(wishProductId);

        ResponseMessage response = ResponseMessage.builder()
                .statusCode(200)
                .resultMessage("관심 상품 삭제 성공").build();

        return ResponseEntity.status(200).body(response);
    }
}
