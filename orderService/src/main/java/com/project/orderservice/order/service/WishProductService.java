package com.project.orderservice.order.service;

import com.project.orderservice.order.dto.ProductDto;
import com.project.orderservice.order.dto.WishProductDto;
import com.project.orderservice.order.entity.WishProduct;
import com.project.orderservice.order.repository.WishProductRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishProductService {

    private final WishProductRepository wishProductRepository;

    public WishProductDto saveWishProduct(Long productId, WishProductDto requestDto, HttpServletRequest request) {
        //TODO Product 를 가져와야 함
        /*
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );
        */
        Long userId = 1L;

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
        Long userId = 1L;

        List<WishProduct> wishProductList = wishProductRepository.findByWishProductByUserId(userId);
        if(wishProductList.isEmpty()) {
            throw new NullPointerException("관심 상품이 없습니다.");
        }

        return wishProductList.stream().map(wishProduct -> WishProductDto.builder()
                .wishProductId(wishProduct.getId())
                .wishQuantity(wishProduct.getWishQuantity())
                /*
                .productDto(ProductDto.builder()
                        .productId(wishProduct.getProduct().getId())
                        .productName(wishProduct.getProduct().getProductName())
                        .imageUrl(wishProduct.getProduct().getImageUrl())
                        .stock(wishProduct.getProduct().getStock())
                        .price(wishProduct.getProduct().getPrice())
                        .detailInfo(wishProduct.getProduct().getDetailInfo()).build())*/.build()).toList();
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