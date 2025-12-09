package com.kosta.somacom.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId; // 우리 시스템의 payment_order_id
    private BigDecimal amount;
    
    
}