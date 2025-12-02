package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.product.ProductCondition;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {
    @NotBlank
    private String name;
    @NotNull @DecimalMin("0.0")
    private BigDecimal price;
    @NotNull @Min(0)
    private Integer stockQuantity;
    private ProductCondition condition;
    private String description;
    private String imageUrl;
}