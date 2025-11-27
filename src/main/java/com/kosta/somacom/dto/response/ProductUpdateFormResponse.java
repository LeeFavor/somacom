package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.product.ProductCondition;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductUpdateFormResponse {
    private Long productId;
    private String baseSpecName;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private ProductCondition condition;
    private String description;

    public ProductUpdateFormResponse(Product product) {
        this.productId = product.getId();
        this.baseSpecName = product.getBaseSpec().getName();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.condition = product.getCondition();
        this.description = product.getDescription();
    }
}