package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.product.Product;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceComparisonDto {
    private Long productId;
    private String productName;
    private String companyName;
    private BigDecimal price;

    public PriceComparisonDto(Product product) {
        this.productId = product.getId();
        this.productName = product.getName();
        this.companyName = product.getSeller().getSellerInfo().getCompanyName();
        this.price = product.getPrice();
    }
}