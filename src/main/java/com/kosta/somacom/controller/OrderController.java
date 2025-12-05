package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.order.dto.InstantOrderRequest;
import com.kosta.somacom.order.dto.OrderDetailResponseDto;
import com.kosta.somacom.order.dto.OrderListResponseDto;
import com.kosta.somacom.order.dto.OrderRequest;
import com.kosta.somacom.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody OrderRequest request,
                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        List<OrderItem> createdItems = orderService.createOrder(userId, request);
        // 생성된 아이템이 있다면, 첫 번째 아이템의 orderId를 반환
        return ResponseEntity.ok(createdItems.isEmpty() ? null : createdItems.get(0).getOrder().getId());
    }

    /**
     * P-202: 즉시 구매 API
     */
    @PostMapping("/instant")
    public ResponseEntity<Long> createInstantOrder(@RequestBody InstantOrderRequest request,
                                                   @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        OrderItem createdItem = orderService.createInstantOrder(userId, request);
        return ResponseEntity.ok(createdItem.getOrder().getId());
    }


    @GetMapping
    public ResponseEntity<Page<OrderListResponseDto>> getOrders(Pageable pageable,
                                                                @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        Page<OrderListResponseDto> orders = orderService.findOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDetailResponseDto> getOrderDetail(@PathVariable Long orderId,
                                                                 @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        OrderDetailResponseDto orderDetail = orderService.findOrder(orderId, userId);
        return ResponseEntity.ok(orderDetail);
    }
}