package com.kosta.somacom.order.dto;

import com.kosta.somacom.domain.order.OrderItem;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class OrderItemDto {
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal priceAtPurchase;

    public OrderItemDto(OrderItem orderItem) {
        this.productId = orderItem.getProduct().getId();
        this.productName = orderItem.getProduct().getName();
        this.quantity = orderItem.getQuantity();
        this.priceAtPurchase = orderItem.getPriceAtPurchase();
    }
}