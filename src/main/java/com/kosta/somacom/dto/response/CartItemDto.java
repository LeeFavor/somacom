package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.cart.CartItem;
import com.kosta.somacom.domain.part.BaseSpec;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItemDto {

    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final String imageUrl;
    private final BigDecimal price;
    private final int quantity;
    private final String sellerName;
    private final String part;

    public CartItemDto(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.imageUrl = cartItem.getProduct().getImage_url();
        this.price = cartItem.getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
        // SellerInfo가 null일 수 있으므로 안전하게 처리
        this.sellerName = cartItem.getProduct().getSeller().getSellerInfo() != null ?
                cartItem.getProduct().getSeller().getSellerInfo().getCompanyName() : "정보 없음";
        this.part = cartItem.getProduct().getBaseSpec().getName();
    }
}