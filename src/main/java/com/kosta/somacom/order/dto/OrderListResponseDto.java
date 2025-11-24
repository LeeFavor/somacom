package com.kosta.somacom.order.dto;

import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class OrderListResponseDto {
    private Long orderId;
    private LocalDateTime orderedAt;
    private String representativeProductName;
    private BigDecimal totalPrice;
    private OrderStatus status;

    public OrderListResponseDto(Order order) {
        this.orderId = order.getId();
        this.orderedAt = order.getOrderedAt();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();

        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            String firstItemName = order.getOrderItems().get(0).getProduct().getName();
            int otherItemsCount = order.getOrderItems().size() - 1;
            this.representativeProductName = otherItemsCount > 0 ?
                    String.format("%s 외 %d건", firstItemName, otherItemsCount) :
                    firstItemName;
        }
    }
}