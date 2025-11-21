package com.kosta.somacom.controller;

import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.cart.dto.CartResponse;
import com.kosta.somacom.service.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<Void> addCartItem(@RequestBody CartItemAddRequest request) {
        // TODO: @AuthenticationPrincipal 등을 통해 실제 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 사용자 ID
        cartService.addCartItem(userId, request);
        return ResponseEntity.ok().build();
    }

    // 내 장바구니 조회
    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        // TODO: @AuthenticationPrincipal 등을 통해 실제 사용자 ID를 가져와야 함
        Long userId = 1L; // 임시 사용자 ID
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    // 장바구니 상품 수량 수정
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<Void> updateCartItemQuantity(@PathVariable Long cartItemId,
                                                       @RequestBody Map<String, Integer> payload) {
        cartService.updateCartItemQuantity(cartItemId, payload.get("quantity"));
        return ResponseEntity.ok().build();
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId) {
        cartService.deleteCartItem(cartItemId);
        return ResponseEntity.ok().build();
    }
}