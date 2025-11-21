package com.kosta.somacom.domain.product;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.user.User;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_spec_id", nullable = false)
    private BaseSpec baseSpec;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stockQuantity;

    @Enumerated(EnumType.STRING)
    // 'condition'은 SQL 예약어이므로 백틱(`)으로 감싸줍니다.
    @Column(name = "`condition`", nullable = false)
    private ProductCondition condition;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee;

    @Lob
    private String description;

    @Column(nullable = false)
    private boolean isVisible = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "image_url")
    private String image_url;

    @Builder
    public Product(BaseSpec baseSpec, User seller, String name, BigDecimal price, int stockQuantity, ProductCondition condition, BigDecimal shippingFee, String description, String img_url) {
        this.baseSpec = baseSpec;
        this.seller = seller;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.condition = condition;
        this.shippingFee = shippingFee;
        this.description = description;
        this.image_url = img_url;
    }
}