package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import lombok.Getter;

@Getter
public class RecommendationResponseDto {

    private final ProductSimpleResponse product;
    private final CompatibilityStatus compatibilityStatus;
    private final String compatibilityReason;

    public RecommendationResponseDto(ProductSimpleResponse product, CompatibilityResult compatibilityResult) {
        this.product = product;
        this.compatibilityStatus = compatibilityResult.getStatus();
        this.compatibilityReason = compatibilityResult.getReasonCode();
    }
}