package com.kosta.somacom.seller.dto;

import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.order.OrderItemStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SellerOrderResponseDto {
    private Long orderItemId;
    private Long orderId;
    private LocalDateTime orderDate;
    private String productName;
    private int quantity;
    private String recipientName;
    private OrderItemStatus status;
    private String trackingNumber;

    public SellerOrderResponseDto(OrderItem orderItem) {
        this.orderItemId = orderItem.getId();
        this.orderId = orderItem.getOrder().getId();
        this.orderDate = orderItem.getOrder().getOrderedAt();
        this.productName = orderItem.getProduct().getName();
        this.quantity = orderItem.getQuantity();
        this.recipientName = orderItem.getOrder().getRecipientName();
        this.status = orderItem.getStatus();
        
        this.trackingNumber = orderItem.getTrackingNumber();
    }
}