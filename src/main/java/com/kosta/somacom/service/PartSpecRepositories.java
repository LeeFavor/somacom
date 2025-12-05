package com.kosta.somacom.service;

import com.kosta.somacom.repository.part.CpuSpecRepository;
import com.kosta.somacom.repository.part.GpuSpecRepository;
import com.kosta.somacom.repository.part.MotherboardSpecRepository;
import com.kosta.somacom.repository.part.RamSpecRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Getter
public class PartSpecRepositories {
    private final CpuSpecRepository cpuSpecRepository;
    private final MotherboardSpecRepository motherboardSpecRepository;
    private final RamSpecRepository ramSpecRepository;
    private final GpuSpecRepository gpuSpecRepository;
    // 추후 다른 부품 Repository 추가 가능
}