package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.product.ProductCondition;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class SellerProductListResponse {
    private Long productId;
    private String baseSpecName;
    private String productName;
    private ProductCondition condition;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;

    public SellerProductListResponse(Product product) {
        this.productId = product.getId();
        this.baseSpecName = product.getBaseSpec().getName();
        this.productName = product.getName();
        this.condition = product.getCondition();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imageUrl = product.getImage_url();
    }
}