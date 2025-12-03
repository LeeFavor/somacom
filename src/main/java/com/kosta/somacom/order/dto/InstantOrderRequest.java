package com.kosta.somacom.order.dto;

import lombok.Data;

@Data
public class InstantOrderRequest {
    private Long productId;
    private int quantity;
    private String recipientName;
    private String shippingAddress;
    private String shippingPostcode;
}