package com.kosta.somacom.service;

import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.cart.dto.CartItemResponse;
import com.kosta.somacom.cart.dto.CartResponse;
import com.kosta.somacom.domain.cart.Cart;
import com.kosta.somacom.domain.cart.CartItem;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.repository.CartRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void addCartItem(Long userId, CartItemAddRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = Cart.createCart(user);
            cartRepository.save(cart);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem savedCartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId());

        if (savedCartItem != null) {
            // 이미 장바구니에 있는 상품이면 수량만 증가
            savedCartItem.addQuantity(request.getQuantity());
        } else {
            // 없는 상품이면 새로 추가
            CartItem cartItem = CartItem.createCartItem(product, request.getQuantity());
            cart.addCartItem(cartItem);
        }
        // 변경 감지(Dirty Checking)에 의해 cartItem의 변경사항이 자동으로 DB에 반영됨
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            // 장바구니가 비어있는 경우
            return new CartResponse(List.of(), "NOT_APPLICABLE", "장바구니가 비어있습니다.");
        }

        List<CartItemResponse> cartItems = cart.getCartItems().stream()
                .map(CartItemResponse::new)
                .collect(Collectors.toList());

        // TODO: SYS-1 호환성 엔진 호출
        // CompatibilityResult result = compatibilityService.check(cart.getCartItems());
        // return new CartResponse(cartItems, result.getStatus(), result.getMessage());

        return new CartResponse(cartItems, "CHECK_PENDING", "호환성 검사 대기 중입니다.");
    }

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));
        cartItem.setQuantity(quantity);
    }

    public void deleteCartItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}