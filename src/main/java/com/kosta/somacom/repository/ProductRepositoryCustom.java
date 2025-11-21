package com.kosta.somacom.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.kosta.somacom.product.dto.request.ProductSearchCondition;
import com.kosta.somacom.product.dto.response.ProductSimpleResponse;

/**
 * QueryDSL을 사용한 동적 상품 검색을 위한 Custom Repository 인터페이스
 */
public interface ProductRepositoryCustom {

    Page<ProductSimpleResponse> search(ProductSearchCondition condition, Pageable pageable);

}