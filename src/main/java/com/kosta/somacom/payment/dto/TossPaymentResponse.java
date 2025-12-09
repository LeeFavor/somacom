package com.kosta.somacom.payment.dto;

import lombok.Data;

@Data
public class TossPaymentResponse {
    private String paymentKey;
    private String method; // e.g., "카드", "토스페이"
}