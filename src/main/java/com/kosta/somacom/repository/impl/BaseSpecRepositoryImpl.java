package com.kosta.somacom.repository.impl;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.dto.request.BaseSpecSearchCondition;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import com.kosta.somacom.repository.BaseSpecRepositoryCustom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.kosta.somacom.domain.part.QBaseSpec.baseSpec;

@RequiredArgsConstructor
public class BaseSpecRepositoryImpl implements BaseSpecRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<BaseSpec> searchBaseSpecs(BaseSpecSearchCondition condition, Pageable pageable) {
        List<BaseSpec> content = queryFactory
                .select(baseSpec)
                .from(baseSpec)
                .where(
                        baseSpec.isDeleted.isFalse(), // 소프트 삭제된 데이터 제외
                        keywordContains(condition.getQuery()),
                        categoryEq(condition.getCategory())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(baseSpec.createdAt.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(baseSpec.count())
                .from(baseSpec)
                .where(
                        baseSpec.isDeleted.isFalse(), // 소프트 삭제된 데이터 제외
                        keywordContains(condition.getQuery()),
                        categoryEq(condition.getCategory())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword) ? baseSpec.name.containsIgnoreCase(keyword).or(baseSpec.id.containsIgnoreCase(keyword)) : null;
    }

    private BooleanExpression categoryEq(PartCategory category) {
        return category != null ? baseSpec.category.eq(category) : null;
    }
}
