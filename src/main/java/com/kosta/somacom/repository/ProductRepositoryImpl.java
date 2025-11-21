package com.kosta.somacom.repository;

import static com.kosta.somacom.domain.product.QBaseSpec.baseSpec;
import static com.kosta.somacom.domain.product.QProduct.product;
import static com.kosta.somacom.domain.user.QSellerInfo.sellerInfo;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import com.kosta.somacom.product.dto.request.ProductSearchCondition;
import com.kosta.somacom.product.dto.response.ProductSimpleResponse;
import com.kosta.somacom.product.dto.response.QProductSimpleResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;


/**
 * ProductRepositoryCustom의 QueryDSL 구현체
 */
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSimpleResponse> search(ProductSearchCondition condition, Pageable pageable) {
        // TODO: 실제 엔티티 구조에 맞게 조인 및 필드 수정 필요
        List<ProductSimpleResponse> content = queryFactory
                .select(new QProductSimpleResponse(
                        product.id,
                        product.name,
                        sellerInfo.companyName,
                        product.price,
                        product.imageUrl // Product 엔티티에 imageUrl 필드 필요
                ))
                .from(product)
                .leftJoin(product.baseSpec, baseSpec)
                .leftJoin(product.seller, sellerInfo.user) // User(Seller)와 SellerInfo 조인
                .where(
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory())
                        // TODO: 상세 필터(condition.getFilters()) 동적 조건 추가
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = content.size(); // TODO: 페이징을 위한 실제 count 쿼리 구현 필요
        return new PageImpl<>(content, pageable, total);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? product.name.containsIgnoreCase(keyword).or(baseSpec.name.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression categoryEq(String category) {
        return StringUtils.hasText(category) ? baseSpec.category.eq(category) : null;
    }
}