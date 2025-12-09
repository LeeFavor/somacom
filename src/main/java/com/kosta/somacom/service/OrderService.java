package com.kosta.somacom.service;

import com.kosta.somacom.domain.cart.CartItem;
import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.order.OrderItemStatus;
import com.kosta.somacom.domain.order.OrderStatus;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.order.dto.InstantOrderRequest;
import com.kosta.somacom.order.dto.OrderDetailResponseDto;
import com.kosta.somacom.order.dto.OrderListResponseDto;
import com.kosta.somacom.order.dto.OrderRequest;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.repository.OrderRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public String createOrder(Long userId, OrderRequest request) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. 주문할 장바구니 아이템들 조회
        List<CartItem> cartItems = cartItemRepository.findAllById(request.getCartItemIds());

        // 3. OrderItem 리스트 생성 (이 과정에서 재고 차감)
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            return OrderItem.builder()
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getProduct().getPrice()) // 주문 시점 가격 기록
                    .status(OrderItemStatus.PENDING)
                    .build();
        }).collect(Collectors.toList());

        // 4. 총 주문 금액 계산
        java.math.BigDecimal totalPrice = orderItems.stream()
                .map(item -> item.getPriceAtPurchase().multiply(new java.math.BigDecimal(item.getQuantity())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // 5. Order 생성 및 OrderItem 연결
        Order order = Order.builder()
                .user(user).totalPrice(totalPrice).status(OrderStatus.PENDING)
                .recipientName(request.getRecipientName()).shippingAddress(request.getShippingAddress()).shippingPostcode(request.getShippingPostcode())
                .build();
        orderItems.forEach(order::addOrderItem);

        // 6. Order 저장 (ID 생성을 위해)
        orderRepository.save(order);

        // 7. payment_order_id 생성 및 저장
        String paymentOrderId = order.getId() + "-" + UUID.randomUUID();
        order.setPaymentOrderId(paymentOrderId);

        // 8. 장바구니에서 주문된 아이템 삭제
        cartItemRepository.deleteAll(cartItems);

        return paymentOrderId;
    }

    /**
     * 즉시 구매 로직 (단일 상품)
     */
    @Transactional
    public String createInstantOrder(Long userId, InstantOrderRequest request) {
        // 1. 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. 상품 조회
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        // 3. OrderItem 생성
        OrderItem orderItem = OrderItem.builder()
                .product(product)
                .quantity(request.getQuantity())
                .priceAtPurchase(product.getPrice())
                .status(OrderItemStatus.PENDING)
                .build();

        // 4. 총 주문 금액 계산
        java.math.BigDecimal totalPrice = orderItem.getPriceAtPurchase().multiply(new java.math.BigDecimal(orderItem.getQuantity()));

        // 5. Order 생성 및 OrderItem 연결
        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(OrderStatus.PENDING)
                .recipientName(request.getRecipientName())
                .shippingAddress(request.getShippingAddress())
                .shippingPostcode(request.getShippingPostcode())
                .build();
        order.addOrderItem(orderItem);

        // 6. Order 저장
        orderRepository.save(order);
        
        // 7. payment_order_id 생성 및 저장
        String paymentOrderId = order.getId() + "-" + UUID.randomUUID();
        order.setPaymentOrderId(paymentOrderId);

        return paymentOrderId;
    }

    public Page<OrderListResponseDto> findOrders(Long userId, Pageable pageable) {
        Page<Order> orders = orderRepository.findOrdersByUserId(userId, pageable);
        return orders.map(OrderListResponseDto::new);
    }

    public OrderDetailResponseDto findOrder(Long orderId, Long userId) {
        Order order = orderRepository.findOrderDetails(orderId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found or access denied."));

        // 한번 더 명시적으로 체크
        if (!order.getUser().getId().equals(userId)) {
            throw new SecurityException("You do not have permission to view this order.");
        }
        return new OrderDetailResponseDto(order);
    }
}