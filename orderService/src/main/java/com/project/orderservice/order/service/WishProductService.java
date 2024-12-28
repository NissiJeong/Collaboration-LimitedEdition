package com.project.orderservice.order.service;

import com.project.orderservice.feignclient.product.ProductFeign;
import com.project.orderservice.order.dto.ProductDto;
import com.project.orderservice.order.dto.WishProductDto;
import com.project.orderservice.order.entity.WishProduct;
import com.project.orderservice.order.repository.WishProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishProductService {

    private final WishProductRepository wishProductRepository;
    private final ProductFeign productFeign;

    public WishProductDto saveWishProduct(Long productId, WishProductDto requestDto, HttpServletRequest request) {
        ProductDto productDto = productFeign.getProduct(productId).orElseThrow(() ->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        // X-Claim-sub 헤더 값을 가져오기
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        WishProduct existWishProduct = wishProductRepository.findByProductIdAndUserId(productId, userId);
        if(existWishProduct != null) {
            throw new IllegalArgumentException("해당 상품은 이미 관심상품에 등록되어 있습니다.");
        }

        // 상품이 존재하면 wish list 저장
        WishProduct wishProduct = new WishProduct(requestDto, productId, userId);
        WishProduct savedWishProduct = wishProductRepository.save(wishProduct);

        return WishProductDto.builder()
                .wishProductId(savedWishProduct.getId())
                .wishQuantity(savedWishProduct.getWishQuantity()).build();
    }

    public List<WishProductDto> getWishProductList(HttpServletRequest request) {
        // X-Claim-sub 헤더 값을 가져오기
        Long userId = Long.parseLong(request.getHeader("X-Claim-sub"));

        List<WishProduct> wishProductList = wishProductRepository.findByWishProductByUserId(userId);
        if(wishProductList.isEmpty()) {
            throw new NullPointerException("관심 상품이 없습니다.");
        }

        List<ProductDto> productDtoList = wishProductList.stream().map(wishProduct -> ProductDto.builder().productId(wishProduct.getProductId()).build()).toList();
        List<ProductDto> productInfoList = productFeign.getProductList(productDtoList);

        return wishProductList.stream().map(wishProduct -> WishProductDto.builder()
                .wishProductId(wishProduct.getId())
                .wishQuantity(wishProduct.getWishQuantity())
                .productDtoList(productInfoList).build()).toList();
    }

    @Transactional
    public WishProductDto updateWishProduct(Long wishProductId, WishProductDto requestDto) {
        WishProduct wishProduct = wishProductRepository.findById(wishProductId).orElseThrow(()->
                new NullPointerException("해당 관심상품이 존재하지 않습니다.")
        );

        if(wishProduct.getWishQuantity() <= 0) {
            throw new IllegalArgumentException("관심상품의 수량은 0개 이하일 수 없습니다.");
        }

        wishProduct.changeWishProduct(requestDto.getWishQuantity());

        return WishProductDto.builder()
                .wishProductId(wishProduct.getId())
                .wishQuantity(wishProduct.getWishQuantity()).build();
    }

    public boolean deleteWishProduct(Long wishProductId) {
        if(wishProductRepository.existsById(wishProductId)) {
            wishProductRepository.deleteById(wishProductId);
            return true;
        }
        return false;
    }
}
