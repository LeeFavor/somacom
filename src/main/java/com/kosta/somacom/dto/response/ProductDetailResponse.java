package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.dto.request.PriceComparisonDto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductDetailResponse {

    // 기본 상품 정보
    private Long productId;
    private String productName;
    private BigDecimal price;
    private Integer stockQuantity;
    private String condition;
    private String description;
    private String imageUrl;

    // 판매자 정보
    private String companyName;

    // 기반 모델 정보
    private String baseSpecName;
    private String manufacturer;

    // 가격 비교 목록
    private List<PriceComparisonDto> priceComparisonList;

    public ProductDetailResponse(Product product, List<PriceComparisonDto> priceComparisonList) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.condition = product.getCondition().name();
        this.description = product.getDescription();
        this.imageUrl = product.getImage_url();
        this.companyName = product.getSeller().getSellerInfo().getCompanyName();

        BaseSpec baseSpec = product.getBaseSpec();
        this.baseSpecName = baseSpec.getName();
        this.manufacturer = baseSpec.getManufacturer();

        this.priceComparisonList = priceComparisonList;
    }
}