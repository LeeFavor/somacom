package com.kosta.somacom.engine.rule.impl;


import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.part.RamSpec;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.engine.rule.CompatibilityRule;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Component
public class MemoryTypeRule implements CompatibilityRule {

    @Override
    public CompatibilityResult check(BaseSpec partA, BaseSpec partB) {
        // CPU <-> Motherboard
        if (isCpuAndMotherboard(partA, partB)) {
            CpuSpec cpu = (partA.getCategory() == PartCategory.CPU) ? partA.getCpuSpec() : partB.getCpuSpec();
            MotherboardSpec mb = (partA.getCategory() == PartCategory.Motherboard) ? partA.getMotherboardSpec() : partB.getMotherboardSpec();

            if (cpu == null || mb == null || !StringUtils.hasText(cpu.getSupportedMemoryTypes())) {
                return CompatibilityResult.success();
            }

            boolean isSupported = Arrays.stream(cpu.getSupportedMemoryTypes().split(","))
                    .anyMatch(type -> type.equalsIgnoreCase(mb.getMemoryType()));

            return isSupported ? CompatibilityResult.success() : new CompatibilityResult(CompatibilityStatus.FAIL, "CPU와 메인보드의 메모리 타입이 일치하지 않습니다.", partA, partB);
        }

        // RAM <-> Motherboard
        if ((partA.getCategory() == PartCategory.RAM && partB.getCategory() == PartCategory.Motherboard) ||
            (partA.getCategory() == PartCategory.Motherboard && partB.getCategory() == PartCategory.RAM)) {
            RamSpec ram = (partA.getCategory() == PartCategory.RAM) ? partA.getRamSpec() : partB.getRamSpec();
            MotherboardSpec mb = (partA.getCategory() == PartCategory.Motherboard) ? partA.getMotherboardSpec() : partB.getMotherboardSpec();

            return ram.getMemoryType().equalsIgnoreCase(mb.getMemoryType()) ? CompatibilityResult.success() : new CompatibilityResult(CompatibilityStatus.FAIL, "RAM과 메인보드의 메모리 타입이 일치하지 않습니다.", partA, partB);
        }

        // [추가] RAM <-> CPU
        if ((partA.getCategory() == PartCategory.RAM && partB.getCategory() == PartCategory.CPU) ||
            (partA.getCategory() == PartCategory.CPU && partB.getCategory() == PartCategory.RAM)) {
            RamSpec ram = (partA.getCategory() == PartCategory.RAM) ? partA.getRamSpec() : partB.getRamSpec();
            CpuSpec cpu = (partA.getCategory() == PartCategory.CPU) ? partA.getCpuSpec() : partB.getCpuSpec();

            if (cpu == null || ram == null || !StringUtils.hasText(cpu.getSupportedMemoryTypes())) {
                return CompatibilityResult.success();
            }

            boolean isSupported = Arrays.stream(cpu.getSupportedMemoryTypes().split(",")).anyMatch(type -> type.equalsIgnoreCase(ram.getMemoryType()));
            return isSupported ? CompatibilityResult.success() : new CompatibilityResult(CompatibilityStatus.FAIL, "RAM과 CPU의 메모리 지원 규격이 일치하지 않습니다.", partA, partB);
        }

        return CompatibilityResult.success();
    }

    private boolean isCpuAndMotherboard(BaseSpec partA, BaseSpec partB) {
        return (partA.getCategory() == PartCategory.CPU && partB.getCategory() == PartCategory.Motherboard) ||
               (partA.getCategory() == PartCategory.Motherboard && partB.getCategory() == PartCategory.CPU);
    }
}