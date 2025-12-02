package com.kosta.somacom.engine.rule.impl;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.engine.rule.CompatibilityRule;
import org.springframework.stereotype.Component;

@Component
public class PcieRule implements CompatibilityRule {

    @Override
    public CompatibilityResult check(BaseSpec partA, BaseSpec partB) {
        // 이 규칙은 GPU와 Motherboard 조합에만 적용됩니다.
        if (!((partA.getCategory() == PartCategory.GPU && partB.getCategory() == PartCategory.Motherboard) ||
              (partA.getCategory() == PartCategory.Motherboard && partB.getCategory() == PartCategory.GPU))) {
            return CompatibilityResult.success();
        }

        GpuSpec gpu = (partA.getCategory() == PartCategory.GPU) ? partA.getGpuSpec() : partB.getGpuSpec();
        MotherboardSpec motherboard = (partA.getCategory() == PartCategory.Motherboard) ? partA.getMotherboardSpec() : partB.getMotherboardSpec();

        // 상세 스펙 정보가 없으면 검사 통과
        if (gpu == null || motherboard == null || motherboard.getPcieVersion() == null || motherboard.getPcieLanes() == null) {
            return new CompatibilityResult(CompatibilityStatus.WARN, "PCIE_SPEC_INFO_MISSING");
        }

        // GPU의 PCIe 버전이 메인보드 슬롯 버전보다 높으면 호환 실패 (예: 5.0 GPU를 4.0 슬롯에 장착 불가 - 성능 저하가 아닌 물리적/인식 문제 가능성 고려)
        // GPU의 PCIe 레인 수가 메인보드 슬롯 레인 수보다 많으면 호환 실패
        boolean versionCompatible = gpu.getPcieVersion().compareTo(motherboard.getPcieVersion()) <= 0;
        boolean lanesCompatible = gpu.getPcieLanes() <= motherboard.getPcieLanes();

        if (versionCompatible && lanesCompatible) {
            return CompatibilityResult.success();
        } else {
            return new CompatibilityResult(CompatibilityStatus.FAIL, "PCIE_MISMATCH");
        }
    }
}