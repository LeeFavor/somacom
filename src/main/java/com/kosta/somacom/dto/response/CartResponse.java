package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class CartResponse {

    private final List<CartItemDto> items;
    private final CompatibilityStatus compatibilityStatus;
    private final String compatibilityReasonCode;
    private final BigDecimal totalPrice;

    public CartResponse(List<CartItemDto> items, CompatibilityResult compatibilityResult) {
        this.items = items;
        this.compatibilityStatus = compatibilityResult.getStatus();
        this.compatibilityReasonCode = compatibilityResult.getReasonCode();
        this.totalPrice = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}