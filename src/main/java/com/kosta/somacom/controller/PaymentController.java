package com.kosta.somacom.controller;

import com.kosta.somacom.order.dto.OrderDetailResponseDto;
import com.kosta.somacom.payment.dto.PaymentConfirmRequest;
import com.kosta.somacom.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/toss/confirm")
    public ResponseEntity<OrderDetailResponseDto> confirmTossPayment(@RequestBody PaymentConfirmRequest request) {
        OrderDetailResponseDto response = paymentService.confirmTossPayment(request);
        return ResponseEntity.ok(response);
    }
}