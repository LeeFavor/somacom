package com.kosta.somacom.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private List<CartItemResponse> cartItems;
    private String compatibilityStatus; // SYS-1 호환성 검사 결과
    private String compatibilityMessage;
}