package com.kosta.somacom.controller;

import com.kosta.somacom.order.dto.OrderDetailResponseDto;
import com.kosta.somacom.order.dto.OrderListResponseDto;
import com.kosta.somacom.order.dto.OrderRequest;
import com.kosta.somacom.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderRequest request) {
        // TODO: @AuthenticationPrincipal 등을 통해 실제 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 사용자 ID
        Long orderId = orderService.createOrder(userId, request);
        return ResponseEntity.ok(orderId);
    }

    @GetMapping
    public ResponseEntity<Page<OrderListResponseDto>> getOrders(Pageable pageable) {
        // TODO: @AuthenticationPrincipal 등을 통해 실제 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 사용자 ID
        Page<OrderListResponseDto> orders = orderService.findOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetail(@PathVariable Long orderId) {
        // TODO: @AuthenticationPrincipal 등을 통해 실제 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 사용자 ID
        OrderDetailResponseDto orderDetail = orderService.findOrder(orderId, userId);
        return ResponseEntity.ok(orderDetail);
    }
}