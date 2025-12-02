package com.kosta.somacom.engine;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.engine.rule.CompatibilityRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 모든 호환성 규칙을 실행하고 최종 결과를 집계하는 서비스
 */
@Service
@RequiredArgsConstructor
public class RuleEngineService {

    // Spring이 @Component로 등록된 모든 CompatibilityRule 구현체를 자동으로 주입합니다.
    private final List<CompatibilityRule> rules;

    public CompatibilityResult checkCompatibility(BaseSpec partA, BaseSpec partB) {
        CompatibilityResult finalResult = CompatibilityResult.success();

        for (CompatibilityRule rule : rules) {
            CompatibilityResult result = rule.check(partA, partB);

            // 가장 심각한 결과를 우선적으로 채택 (FAIL > WARN > SUCCESS)
            if (result.getStatus() == CompatibilityStatus.FAIL) {
                return result; // FAIL이 나오면 즉시 검사를 중단하고 FAIL 결과를 반환
            }
            if (result.getStatus() == CompatibilityStatus.WARN) {
                finalResult = result; // WARN은 일단 저장해두고, 다른 FAIL이 없는지 계속 검사
            }
        }
        return finalResult; // 모든 규칙을 통과했거나, WARN만 있었던 경우
    }
}