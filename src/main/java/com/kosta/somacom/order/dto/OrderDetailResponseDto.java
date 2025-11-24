package com.kosta.somacom.order.dto;

import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class OrderDetailResponseDto {
    private Long orderId;
    private LocalDateTime orderedAt;
    private BigDecimal totalPrice;
    private OrderStatus status;
    private String recipientName;
    private String shippingAddress;
    private String shippingPostcode;
    private List<OrderItemDto> orderItems;

    public OrderDetailResponseDto(Order order) {
        this.orderId = order.getId();
        this.orderedAt = order.getOrderedAt();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
        this.recipientName = order.getRecipientName();
        this.shippingAddress = order.getShippingAddress();
        this.shippingPostcode = order.getShippingPostcode();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
    }
}