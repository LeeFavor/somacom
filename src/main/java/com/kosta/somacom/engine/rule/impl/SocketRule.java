package com.kosta.somacom.engine.rule.impl;


import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.engine.rule.CompatibilityRule;
import org.springframework.stereotype.Component;

@Component
public class SocketRule implements CompatibilityRule {

    @Override
    public CompatibilityResult check(BaseSpec partA, BaseSpec partB) {
        // 이 규칙은 CPU와 Motherboard 조합에만 적용됩니다.
        if (!((partA.getCategory() == PartCategory.CPU && partB.getCategory() == PartCategory.Motherboard) ||
              (partA.getCategory() == PartCategory.Motherboard && partB.getCategory() == PartCategory.CPU))) {
            return CompatibilityResult.success(); // 해당 없으면 성공으로 간주
        }

        CpuSpec cpu = (partA.getCategory() == PartCategory.CPU) ? partA.getCpuSpec() : partB.getCpuSpec();
        MotherboardSpec motherboard = (partA.getCategory() == PartCategory.Motherboard) ? partA.getMotherboardSpec() : partB.getMotherboardSpec();

        if (cpu == null || motherboard == null) {
            return new CompatibilityResult(CompatibilityStatus.WARN, "SPEC_INFO_MISSING");
        }

        if (cpu.getSocket().equalsIgnoreCase(motherboard.getSocket())) {
            return CompatibilityResult.success();
        } else {
            return new CompatibilityResult(CompatibilityStatus.FAIL, "SOCKET_MISMATCH");
        }
    }
}