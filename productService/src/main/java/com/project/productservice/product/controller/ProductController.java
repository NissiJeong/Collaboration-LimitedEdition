package com.project.productservice.product.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.common.dto.ResponseMessage;
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
    public ResponseEntity<ResponseMessage> registerProduct(@RequestBody ProductDto requestDto) throws JsonProcessingException {
        ProductDto responseDto = productService.saveProduct(requestDto);

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDto)
                .statusCode(201)
                .resultMessage("상품등록 성공").build();

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public ResponseEntity<ResponseMessage> getProductList() {
        List<ProductDto> responseDtoList = productService.getProductList("N");

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDtoList)
                .statusCode(200)
                .resultMessage("일반 상품 목록 조회 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @GetMapping(value = "/event")
    public ResponseEntity<ResponseMessage> getEvenetProductList() {
        List<ProductDto> responseDtoList = productService.getProductList("Y");

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDtoList)
                .statusCode(200)
                .resultMessage("이벤트 상품 목록 조회 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @GetMapping(value = "/detail/{productId}")
    public ResponseEntity<ResponseMessage> getProductDetail(@PathVariable Long productId) {
        ProductDto responseDto = productService.getProductDetail(productId);

        ResponseMessage response = ResponseMessage.builder()
                .data(responseDto)
                .statusCode(200)
                .resultMessage("상품조회 성공").build();

        return ResponseEntity.status(200).body(response);
    }

    @GetMapping(value = "/detail/stock/{productId}")
    public ResponseEntity<ResponseMessage> getProductDetailStock(@PathVariable Long productId) {
        Integer productStock = productService.getProductDetailStock(productId);

        ResponseMessage responseMessage = ResponseMessage.builder()
                .data(productStock)
                .statusCode(200)
                .resultMessage("상품 수량 조회 성공").build();

        return ResponseEntity.status(200).body(responseMessage);
    }

    @GetMapping(value = "/{productId}")
    public ResponseEntity<?> getProduct(@PathVariable Long productId) {
        ProductDto responseDto = productService.getProductDetail(productId);
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping(value = "/stock/{productId}")
    public ResponseEntity<?> changeProductStockByOrder(@PathVariable Long productId, @RequestBody ProductDto productDto) {
        ProductDto responseDto = productService.changeProductStockByOrder(productId, productDto, "minus");
        return ResponseEntity.ok(responseDto);
    }

    @PutMapping(value = "/stock/cancel/{productId}")
    public ResponseEntity<?> plusProductStockByOrderCancel(@PathVariable Long productId, @RequestBody ProductDto productDto) {
        ProductDto responseDto = productService.changeProductStockByOrder(productId, productDto, "plus");
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping(value = "/bulk")
    public ResponseEntity<?> getProductList(@RequestBody List<ProductDto> productDtoList) {
        List<ProductDto> responseDtoList = productService.getProductList(productDtoList);
        return ResponseEntity.ok(responseDtoList);
    }
}
