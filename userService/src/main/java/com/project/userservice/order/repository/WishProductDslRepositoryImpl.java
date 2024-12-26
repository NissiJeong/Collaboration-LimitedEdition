package com.project.userservice.order.repository;

import com.project.userservice.order.entity.QWishProduct;
import com.project.userservice.order.entity.WishProduct;
import com.project.userservice.product.entity.QProduct;
import com.project.userservice.user.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class WishProductDslRepositoryImpl implements WishProductDslRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<WishProduct> findByWishProductByUser(User user) {
        QWishProduct wishProduct = QWishProduct.wishProduct;
        QProduct product = QProduct.product;

        return jpaQueryFactory.selectFrom(wishProduct)
                .join(wishProduct.product, product).fetchJoin() // Product 조인
                .where(wishProduct.user.id.eq(user.getId())) // 조건 사용 가능
                .fetch();
    }
}