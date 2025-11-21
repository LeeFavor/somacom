package com.kosta.somacom.service;

import com.kosta.somacom.domain.cart.CartItem;
import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.order.OrderItemStatus;
import com.kosta.somacom.domain.order.OrderStatus;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.order.dto.OrderRequest;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.repository.OrderRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public Long createOrder(Long userId, OrderRequest request) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. 주문할 장바구니 아이템들 조회
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

        // 3. OrderItem 리스트 생성 (이 과정에서 재고 차감)
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            cartItem.getProduct().removeStock(cartItem.getQuantity()); // 재고 차감
            return OrderItem.builder()
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice()) // 주문 시점 가격 기록
                    .status(OrderItemStatus.PAID)
                    .build();
        }).collect(Collectors.toList());

        // 4. 총 주문 금액 계산
        BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 5. Order 생성 및 OrderItem 연결
        Order order = Order.builder()
                .user(user).totalPrice(totalPrice).status(OrderStatus.PAID)
                .recipientName(request.getRecipientName()).shippingAddress(request.getShippingAddress()).shippingPostcode(request.getShippingPostcode())
                .build();
        orderItems.forEach(order::addOrderItem);

        // 6. Order 저장 (Cascade 설정으로 OrderItem도 함께 저장됨)
        orderRepository.save(order);

        // 7. 장바구니에서 주문된 아이템 삭제
        cartItemRepository.deleteAll(cartItems);

        return order.getId();
    }
}