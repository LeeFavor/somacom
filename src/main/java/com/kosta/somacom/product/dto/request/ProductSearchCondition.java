package com.kosta.somacom.product.dto.request;

import lombok.Data;

import java.util.Map;

/**
 * 상품 검색 조건을 담는 DTO (P-201-SEARCH)
 * /api/products/search 의 요청 파라미터를 매핑합니다.
 */
@Data
public class ProductSearchCondition {

    private String keyword;
    private String category;
    private Map<String, String> filters;

}