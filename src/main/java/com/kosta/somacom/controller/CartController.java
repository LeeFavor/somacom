package com.kosta.somacom.controller;

import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.cart.dto.CartItemDeleteRequest;
import com.kosta.somacom.cart.dto.CartResponse;
import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.service.CartService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // 장바구니에 상품 추가
    @PostMapping("/items")
    public ResponseEntity<Void> addCartItem(@RequestBody CartItemAddRequest request,
                                            @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        cartService.addCartItem(userId, request);
        return ResponseEntity.ok().build();
    }

    // 내 장바구니 조회
    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
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
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long cartItemId,
                                               @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        cartService.deleteCartItem(cartItemId, userId);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/items")
    public ResponseEntity<Void> deleteCartItems(@RequestBody CartItemDeleteRequest request,
                                                  @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long userId = principalDetails.getUser().getId();
        cartService.deleteCartItems(request.getCartItemIds(), userId);
        return ResponseEntity.noContent().build();
    }
}