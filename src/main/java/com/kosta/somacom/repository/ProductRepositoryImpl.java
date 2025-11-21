package com.kosta.somacom.repository;

import static com.kosta.somacom.domain.part.QBaseSpec.baseSpec;
import static com.kosta.somacom.domain.part.QCpuSpec.cpuSpec;
import static com.kosta.somacom.domain.product.QProduct.product;
import static com.kosta.somacom.domain.user.QSellerInfo.sellerInfo;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * ProductRepositoryCustom의 QueryDSL 구현체
 */
@Repository
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ProductSimpleResponse> search(ProductSearchCondition condition, Pageable pageable) {
        List<ProductSimpleResponse> content = queryFactory
                .select(Projections.constructor(ProductSimpleResponse.class,
                        product.id,
                        product.name,
                        sellerInfo.companyName,
                        product.price,
                        product.image_url
                ))
                .from(baseSpec) // 기준 테이블을 baseSpec으로 변경
                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id)) // baseSpec에 해당하는 product를 LEFT JOIN
                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id)) // product에 연결된 sellerInfo를 LEFT JOIN
                // 상세 스펙 테이블들은 필터 조건이 있을 때만 동적으로 조인
                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.id))
                .where(
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        dynamicFilters(condition.getFilters()) // 상세 필터 동적 적용
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // Count 쿼리 (성능을 위해 content.size() 대신 별도 실행)
        JPAQuery<Long> countQuery = queryFactory
                .select(product.count())
                .from(baseSpec)
                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id))
                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id))
                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.id))
                .where(
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        dynamicFilters(condition.getFilters())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        // 상품명 또는 기반모델명에서 키워드 검색
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        // product가 LEFT JOIN으로 인해 null일 수 있으므로, product.name 검색은 product가 null이 아닐 때만 수행
        BooleanExpression productNameContains = product.isNotNull().and(product.name.containsIgnoreCase(keyword));
        return productNameContains.or(baseSpec.name.containsIgnoreCase(keyword));
    }

    private BooleanExpression categoryEq(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        try {
            return baseSpec.category.eq(PartCategory.valueOf(category.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return null; // or log an error for invalid category string
        }
    }

    // 상세 필터들을 동적으로 조합하는 메소드
    private BooleanBuilder dynamicFilters(Map<String, String> filters) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filters == null || filters.isEmpty()) {
            return null;
        }

        // 예시: CPU 소켓 필터
        if (StringUtils.hasText(filters.get("socket"))) {
            builder.and(cpuSpec.socket.equalsIgnoreCase(filters.get("socket")));
        }
        // 여기에 다른 필터 조건들을 추가 (e.g., manufacturer, memoryType 등)

        return builder;
    }
}