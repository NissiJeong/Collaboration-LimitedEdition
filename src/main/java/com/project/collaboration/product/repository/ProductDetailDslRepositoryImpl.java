package com.project.collaboration.product.repository;

import com.project.collaboration.product.entity.ProductDetail;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.project.collaboration.product.entity.QProductDetail.productDetail;

@Repository
@RequiredArgsConstructor
public class ProductDetailDslRepositoryImpl implements ProductDetailDslRepository{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public ProductDetail findByProductAndVersion(Long productId, int version) {
        return jpaQueryFactory.selectFrom(productDetail)
                .leftJoin(productDetail.product).fetchJoin()
                .where(productDetail.version.eq(version)
                        .and(productDetail.product.id.eq(productId)))
                .fetchOne();
    }
}
