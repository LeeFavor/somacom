package com.kosta.somacom.controller;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.seller.dto.SellerOrderItemUpdateRequest;
import com.kosta.somacom.seller.dto.SellerOrderResponseDto;
import com.kosta.somacom.service.SellerOrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/seller/orders")
@RequiredArgsConstructor
public class SellerOrderController {

    private final SellerOrderService sellerOrderService;

    @GetMapping
    public ResponseEntity<Page<SellerOrderResponseDto>> getOrdersForSeller(
            @AuthenticationPrincipal PrincipalDetails principalDetails, Pageable pageable) {
        Long sellerId = principalDetails.getUser().getId();
        Page<SellerOrderResponseDto> orders = sellerOrderService.getOrdersForSeller(sellerId, pageable);
        return ResponseEntity.ok(orders);
    }
    @PutMapping("/{orderItemId}")
    public ResponseEntity<Void> updateOrderItemStatus(
            @PathVariable Long orderItemId,
            @Valid @RequestBody SellerOrderItemUpdateRequest request,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        sellerOrderService.updateOrderItemStatus(orderItemId, sellerId, request);
        return ResponseEntity.ok().build();
    }
}