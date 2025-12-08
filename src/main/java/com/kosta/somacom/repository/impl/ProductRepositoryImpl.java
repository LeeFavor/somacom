package com.kosta.somacom.repository.impl;

import static com.kosta.somacom.domain.part.QBaseSpec.baseSpec;
import static com.kosta.somacom.domain.part.QCpuSpec.cpuSpec;
import static com.kosta.somacom.domain.part.QGpuSpec.gpuSpec;
import static com.kosta.somacom.domain.part.QMotherboardSpec.motherboardSpec;
import static com.kosta.somacom.domain.part.QRamSpec.ramSpec;
import static com.kosta.somacom.domain.product.QProduct.product;
import static com.kosta.somacom.domain.score.QCompatibilityScore.compatibilityScore;
import static com.kosta.somacom.domain.user.QSellerInfo.sellerInfo;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.repository.CartRepository;
import com.kosta.somacom.repository.ProductRepositoryCustom;
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
    private final CartRepository cartRepository; // 호환성 필터를 위해 주입

//    @Override
//    public Page<ProductSimpleResponse> search(ProductSearchCondition condition, Pageable pageable) {
//        List<ProductSimpleResponse> content = queryFactory
//                .select(Projections.constructor(ProductSimpleResponse.class,
//                        product.id,
//                        product.name,
//                        sellerInfo.companyName,
//                        product.price,
//                        product.image_url
//                ))
//                .from(baseSpec) // 기준 테이블을 baseSpec으로 변경
//                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id)) // baseSpec에 해당하는 product를 LEFT JOIN
//                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id)) // product에 연결된 sellerInfo를 LEFT JOIN
//                // 상세 스펙 테이블들은 필터 조건이 있을 때만 동적으로 조인
//                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.baseSpec.id))
//                .leftJoin(motherboardSpec).on(baseSpec.id.eq(motherboardSpec.baseSpec.id))
//                .leftJoin(ramSpec).on(baseSpec.id.eq(ramSpec.baseSpec.id))
//                .leftJoin(gpuSpec).on(baseSpec.id.eq(gpuSpec.baseSpec.id))
//                .where(
//                		product.isVisible.isTrue(), // Soft Delete된 상품 제외
//                        keywordContains(condition.getKeyword()),
//                        categoryEq(condition.getCategory()),
//                        compatibilityFilter(condition.isCompatFilter(), condition.getUserId()), // 호환성 필터 적용
//                        dynamicFilters(condition.getFilters()) // 상세 필터 동적 적용
//                )
//                .offset(pageable.getOffset())
//                .limit(pageable.getPageSize())
//                .fetch();
//
//        // Count 쿼리 (성능을 위해 content.size() 대신 별도 실행)
//        JPAQuery<Long> countQuery = queryFactory
//                .select(product.count())
//                .from(baseSpec)
//                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id))
//                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id))
//                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.baseSpec.id))
//                .leftJoin(motherboardSpec).on(baseSpec.id.eq(motherboardSpec.baseSpec.id))
//                .leftJoin(ramSpec).on(baseSpec.id.eq(ramSpec.baseSpec.id))
//                .leftJoin(gpuSpec).on(baseSpec.id.eq(gpuSpec.baseSpec.id))
//                .where(
//                        keywordContains(condition.getKeyword()),
//                        categoryEq(condition.getCategory()),
//                        compatibilityFilter(condition.isCompatFilter(), condition.getUserId()),
//                        dynamicFilters(condition.getFilters())
//                );
//
//        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
//    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        // baseSpec.name은 항상 존재.
        BooleanExpression baseSpecMatch = baseSpec.name.containsIgnoreCase(keyword);

        // product.name은 LEFT JOIN으로 인해 null일 수 있으므로, isNotNull 체크와 함께 사용.
        BooleanExpression productMatch = product.name.isNotNull().and(product.name.containsIgnoreCase(keyword));

        // 두 조건을 OR로 결합.
        return baseSpecMatch.or(productMatch);
    }

    private BooleanExpression categoryEq(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        try {
            return baseSpec.category.eq(PartCategory.valueOf(category));
        } catch (IllegalArgumentException e) {
            return null; // or log an error for invalid category string
        }
    }

    // P-203: 호환성 필터
    private BooleanBuilder compatibilityFilter(boolean isCompatFilter, Long userId) {
        if (!isCompatFilter || userId == null) {

        	System.out.println("111111111111111필터링 실패");
            return new BooleanBuilder(); // 빈 Builder 반환
        }

        List<BaseSpec> itemsInCart = cartRepository.findBaseSpecsInCartByUserId(userId);
        if (itemsInCart.isEmpty()) {
            return new BooleanBuilder(); // 빈 Builder 반환
        }

        BooleanBuilder compatibilityBuilder = new BooleanBuilder();

        // 장바구니의 모든 아이템과 호환되어야 함
        for (BaseSpec itemInCart : itemsInCart) {
            // 검색 대상(baseSpec)이 장바구니 아이템과 FAIL 관계가 아니어야 함
            BooleanExpression isNotFail = compatibilityScore.status.ne(CompatibilityStatus.FAIL);

            // (A, B) 와 (B, A) 양방향으로 체크
            BooleanExpression pair1 = compatibilityScore.specAId.eq(baseSpec.id).and(compatibilityScore.specBId.eq(itemInCart.getId()));
            //BooleanExpression pair2 = compatibilityScore.specAId.eq(itemInCart.getId()).and(compatibilityScore.specBId.eq(baseSpec.id));

            // 장바구니의 각 아이템에 대해, 호환성 점수 테이블에 FAIL이 아닌 레코드가 존재하는지 확인
            compatibilityBuilder.and(
                queryFactory.selectFrom(compatibilityScore)
                    .where(
                        (pair1).and(isNotFail)
                    ).exists()
            );
        }

        return compatibilityBuilder;
    }

    // 상세 필터들을 동적으로 조합하는 메소드
    private BooleanBuilder dynamicFilters(Map<String, String> filters, String searchCategory) {
        BooleanBuilder builder = new BooleanBuilder();
        if (filters == null || filters.isEmpty()) {
        	
            return builder;
        }

        // --- CPU 상세 필터 ---
        if (StringUtils.hasText(filters.get("socket"))) {
            builder.and(baseSpec.category.eq(PartCategory.CPU)); // 카테고리 강제
            builder.and(cpuSpec.socket.equalsIgnoreCase(filters.get("socket")));
        }
        if (StringUtils.hasText(filters.get("hasIgpu"))) {
            builder.and(baseSpec.category.eq(PartCategory.CPU)); // 카테고리 강제
            builder.and(cpuSpec.hasIgpu.eq(Boolean.valueOf(filters.get("hasIgpu"))));
        }

        // --- Motherboard 상세 필터 ---
        if (StringUtils.hasText(filters.get("chipset"))) {
            builder.and(baseSpec.category.eq(PartCategory.Motherboard)); // 카테고리 강제
            builder.and(motherboardSpec.chipset.equalsIgnoreCase(filters.get("chipset")));
        }
        if (StringUtils.hasText(filters.get("formFactor"))) {
            builder.and(baseSpec.category.eq(PartCategory.Motherboard)); // 카테고리 강제
            builder.and(motherboardSpec.formFactor.equalsIgnoreCase(filters.get("formFactor")));
        }

        // --- RAM 상세 필터 ---
        if (StringUtils.hasText(filters.get("speedMhz"))) {
            builder.and(baseSpec.category.eq(PartCategory.RAM)); // 카테고리 강제
            builder.and(ramSpec.speedMhz.eq(Integer.valueOf(filters.get("speedMhz"))));
        }
        if (StringUtils.hasText(filters.get("capacityGb"))) {
            builder.and(baseSpec.category.eq(PartCategory.RAM)); // 카테고리 강제
            builder.and(ramSpec.capacityGb.eq(Integer.valueOf(filters.get("capacityGb"))));
        }

        // --- GPU 상세 필터 ---
        if (StringUtils.hasText(filters.get("pcieVersion"))) {
            builder.and(baseSpec.category.eq(PartCategory.GPU)); // 카테고리 강제
            // pcieVersion 필드는 BigDecimal 타입이므로, new BigDecimal()로 변환해야 합니다.
            builder.and(gpuSpec.pcieVersion.eq(new java.math.BigDecimal(filters.get("pcieVersion"))));
        }

        // --- 공통 필터 (여러 부품에 걸쳐 있을 수 있음) ---
        if (StringUtils.hasText(filters.get("memoryType"))) {
            String memoryType = filters.get("memoryType");
            BooleanBuilder memoryTypeBuilder = new BooleanBuilder(); // OR 조건을 위한 별도 Builder

            // 검색 카테고리에 맞는 필드만 정확히 AND 조건으로 비교
            if ("CPU".equalsIgnoreCase(searchCategory)) {
                memoryTypeBuilder.or(cpuSpec.supportedMemoryTypes.containsIgnoreCase(memoryType));
            } else if ("Motherboard".equalsIgnoreCase(searchCategory)) {
                memoryTypeBuilder.or(motherboardSpec.memoryType.equalsIgnoreCase(memoryType));
            } else if ("RAM".equalsIgnoreCase(searchCategory)) {
                memoryTypeBuilder.or(ramSpec.memoryType.equalsIgnoreCase(memoryType));
            } else { // 카테고리 지정이 없으면 모든 관련 필드 OR 검색 (기존 방식 유지)
                memoryTypeBuilder.or(cpuSpec.supportedMemoryTypes.containsIgnoreCase(memoryType));
                memoryTypeBuilder.or(motherboardSpec.memoryType.equalsIgnoreCase(memoryType));
                memoryTypeBuilder.or(ramSpec.memoryType.equalsIgnoreCase(memoryType));
            }
            builder.and(memoryTypeBuilder);
        }

        return builder;
    }

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
                .from(baseSpec)
                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id))
                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id))
                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.baseSpec.id))
                .leftJoin(motherboardSpec).on(baseSpec.id.eq(motherboardSpec.baseSpec.id))
                .leftJoin(ramSpec).on(baseSpec.id.eq(ramSpec.baseSpec.id))
                .leftJoin(gpuSpec).on(baseSpec.id.eq(gpuSpec.baseSpec.id))
                .where(
                        product.isVisible.isTrue(),
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        compatibilityFilter(condition.isCompatFilter(), condition.getUserId()), // 리팩토링된 호환성 필터 적용
                        dynamicFilters(condition.getFilters(), condition.getCategory())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(baseSpec.count()) // product.count() -> baseSpec.count()로 수정
                .from(baseSpec)
                .leftJoin(product).on(baseSpec.id.eq(product.baseSpec.id))
                .leftJoin(sellerInfo).on(product.seller.id.eq(sellerInfo.user.id))
                .leftJoin(cpuSpec).on(baseSpec.id.eq(cpuSpec.baseSpec.id))
                .leftJoin(motherboardSpec).on(baseSpec.id.eq(motherboardSpec.baseSpec.id))
                .leftJoin(ramSpec).on(baseSpec.id.eq(ramSpec.baseSpec.id))
                .leftJoin(gpuSpec).on(baseSpec.id.eq(gpuSpec.baseSpec.id))
                .where(
                        product.isVisible.isTrue(),
                        keywordContains(condition.getKeyword()),
                        categoryEq(condition.getCategory()),
                        compatibilityFilter(condition.isCompatFilter(), condition.getUserId()),
                        dynamicFilters(condition.getFilters(), condition.getCategory())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }
}