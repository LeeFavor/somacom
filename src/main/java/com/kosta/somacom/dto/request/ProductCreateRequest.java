package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.product.ProductCondition;
import com.kosta.somacom.domain.user.User;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
public class ProductCreateRequest {

    @NotBlank(message = "기반 모델 ID는 필수입니다.")
    private String baseSpecId;

    @NotBlank(message = "판매 상품명은 필수입니다.")
    private String name;

    @NotNull(message = "판매가는 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private BigDecimal price;

    @NotNull(message = "재고는 필수입니다.")
    @Min(value = 0, message = "재고는 0 이상이어야 합니다.")
    private int stockQuantity;

    @NotNull(message = "상품 상태는 필수입니다.")
    private ProductCondition condition;

    // 배송비 필드 추가 (기본값 0)
    private BigDecimal shippingFee = BigDecimal.ZERO;
    
    private String imageUrl;


    public Product toEntity(BaseSpec baseSpec, User seller) {
        return Product.builder()
                .baseSpec(baseSpec)
                .seller(seller)
                .name(this.name)
                .price(this.price)
                .stockQuantity(this.stockQuantity)
                .condition(this.condition)
                .shippingFee(this.shippingFee)
                .img_url(imageUrl)
                .build();
    }
}