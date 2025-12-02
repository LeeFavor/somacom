package com.kosta.somacom.engine.rule;


import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.score.CompatibilityStatus;

import java.util.Map;

/**
 * 두 부품 간의 호환성을 검사하는 모든 규칙의 공통 인터페이스
 */
public interface CompatibilityRule {

    /**
     * 두 부품의 호환성을 검사합니다.
     * @param partA 첫 번째 부품
     * @param partB 두 번째 부품
     * @return 호환성 검사 결과 (상태와 사유 코드 포함)
     */
    CompatibilityResult check(BaseSpec partA, BaseSpec partB);
}