package com.project.orderservice.order.repository;

import com.project.orderservice.order.entity.QWishProduct;
import com.project.orderservice.order.entity.WishProduct;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WishProductDslRepositoryImpl implements WishProductDslRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WishProduct> findByWishProductByUserId(Long userId) {
        QWishProduct wishProduct = QWishProduct.wishProduct;

        return jpaQueryFactory.selectFrom(wishProduct)
                .where(wishProduct.userId.eq(userId)) // 조건 사용 가능
                .fetch();
    }
}
