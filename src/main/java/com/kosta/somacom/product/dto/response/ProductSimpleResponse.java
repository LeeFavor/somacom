package com.kosta.somacom.product.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 검색 결과 목록의 개별 아이템 DTO (P-201-SEARCH)
 * QueryDSL Projection을 사용하여 조회 성능을 최적화합니다.
 */
@Data
@NoArgsConstructor
public class ProductSimpleResponse {

    private Long productId;
    private String productName;
    private String sellerName;
    private Long price;
    private String imageUrl;

    @QueryProjection
    public ProductSimpleResponse(Long productId, String productName, String sellerName, Long price, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.sellerName = sellerName;
        this.price = price;
        this.imageUrl = imageUrl;
    }
}