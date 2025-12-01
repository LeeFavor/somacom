package com.kosta.somacom.cart.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartItemDeleteRequest {
    private List<Long> cartItemIds;
}