package com.kosta.somacom.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private List<Long> cartItemIds; // 주문할 장바구니 아이템 ID 목록
    private String recipientName;
    private String shippingAddress;
    private String shippingPostcode;
}