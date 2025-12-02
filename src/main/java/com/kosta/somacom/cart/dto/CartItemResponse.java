package com.kosta.somacom.cart.dto;

import com.kosta.somacom.domain.cart.CartItem;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private String imageUrl;

    public CartItemResponse(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.price = cartItem.getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
        this.imageUrl = cartItem.getProduct().getImage_url();
    }
}