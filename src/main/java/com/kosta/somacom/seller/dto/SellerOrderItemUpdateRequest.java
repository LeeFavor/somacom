package com.kosta.somacom.seller.dto;

import com.kosta.somacom.domain.order.OrderItemStatus;
import lombok.Data;

@Data
public class SellerOrderItemUpdateRequest {
    private OrderItemStatus status;
    private String trackingNumber;
}