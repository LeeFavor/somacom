package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.dto.request.BaseSpecCreateRequest;
import com.kosta.somacom.repository.BaseSpecRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPartService {

    private final BaseSpecRepository baseSpecRepository;

    @Transactional
    public String createBaseSpec(BaseSpecCreateRequest request) {
        // 1. Service 계층에서 ID 생성
        String generatedId = "base_" +
                request.getCategory().name().toLowerCase() + "_" +
                request.getManufacturer().toLowerCase().replaceAll("\\s+", "") + "_" +
                request.getName().toLowerCase().replaceAll("\\s+", "-") + "_" +
                UUID.randomUUID().toString().substring(0, 8);

        // DTO를 Entity로 변환
        BaseSpec baseSpec = request.toEntity(generatedId);

        // BaseSpec을 저장합니다.
        // 연관된 하위 스펙(CpuSpec 등)은 CascadeType.ALL 설정에 의해 함께 저장됩니다.
        BaseSpec savedBaseSpec = baseSpecRepository.save(baseSpec);

        return savedBaseSpec.getId();
    }
}