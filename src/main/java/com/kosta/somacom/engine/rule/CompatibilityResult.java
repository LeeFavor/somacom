package com.kosta.somacom.engine.rule;


import com.kosta.somacom.domain.score.CompatibilityStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CompatibilityResult {

    private final CompatibilityStatus status;
    private final String reasonCode;

    public static CompatibilityResult success() {
        return new CompatibilityResult(CompatibilityStatus.SUCCESS, null);
    }
}