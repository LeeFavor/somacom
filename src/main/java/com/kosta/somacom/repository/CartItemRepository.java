package com.kosta.somacom.repository;

import com.kosta.somacom.domain.cart.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 장바구니에 특정 상품이 이미 담겨 있는지 확인하기 위한 메소드
    CartItem findByCart_IdAndProduct_Id(Long cartId, Long productId);
}