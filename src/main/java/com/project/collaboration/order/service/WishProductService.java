package com.project.collaboration.order.service;

import com.project.collaboration.order.dto.WishProductDto;
import com.project.collaboration.order.entity.WishProduct;
import com.project.collaboration.order.repository.WishProductRepository;
import com.project.collaboration.product.entity.Product;
import com.project.collaboration.product.repository.ProductRepository;
import com.project.collaboration.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishProductService {

    private final WishProductRepository wishProductRepository;
    private final ProductRepository productRepository;

    public WishProductDto saveWishProduct(Long productId, WishProductDto requestDto, UserDetailsImpl userDetails) {
        Product product = productRepository.findById(productId).orElseThrow(() ->
                new NullPointerException("해당 상품이 존재하지 않습니다.")
        );

        //wishProductRepository.findByProductAnd

        // 상품이 존재하면 wish list 저장
        WishProduct wishProduct = new WishProduct(requestDto, product, userDetails.getUser());
        WishProduct savedWishProduct = wishProductRepository.save(wishProduct);

        return new WishProductDto().builder()
                .wishProductId(savedWishProduct.getId())
                .wishQuantity(savedWishProduct.getWishQuantity()).build();
    }
}
