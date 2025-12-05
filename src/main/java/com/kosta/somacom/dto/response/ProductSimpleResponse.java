package com.kosta.somacom.dto.response;

import java.math.BigDecimal;

import com.kosta.somacom.domain.product.Product;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductSimpleResponse {
    private Long productId;
    private String productName;
    private String companyName; // 판매자 상호명
    private BigDecimal price;
    private String imageUrl;

    @QueryProjection
    public ProductSimpleResponse(Long productId, String productName, String companyName, BigDecimal price, String imageUrl) {
        this.productId = productId;
        this.productName = productName;
        this.companyName = companyName;
        this.price = price;
        this.imageUrl = imageUrl;
    }
    
    public ProductSimpleResponse(Product p) {
        this.productId = p.getId();
        this.productName = p.getName();
        this.companyName = p.getSeller().getSellerInfo().getCompanyName();
        this.price = p.getPrice();
        this.imageUrl = p.getImage_url();
    }
}
