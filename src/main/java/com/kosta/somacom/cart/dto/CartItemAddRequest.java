package com.kosta.somacom.cart.dto;

import lombok.Data;

@Data
public class CartItemAddRequest {
    private Long productId;
    private int quantity;
}