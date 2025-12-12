package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.RamSpec;
import com.kosta.somacom.dto.request.BaseSpecCreateRequest;
import com.kosta.somacom.dto.request.BaseSpecSearchCondition;
import com.kosta.somacom.dto.request.BaseSpecUpdateRequest;
import com.kosta.somacom.dto.response.BaseSpecDetailResponse;
import com.kosta.somacom.dto.response.BaseSpecListResponse;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.CompatibilityScoreRepository;
import com.kosta.somacom.repository.PopularityScoreRepository;
import com.kosta.somacom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminPartService {

    private final BaseSpecRepository baseSpecRepository;
    private final ProductRepository productRepository;
    private final CompatibilityScoreRepository compatibilityScoreRepository;
    private final PopularityScoreRepository popularityScoreRepository;

    @Transactional
    public String createBaseSpec(BaseSpecCreateRequest request) {
        // 1. Service 계층에서 ID 생성
        String generatedId = "base_" +
                request.getCategory().name().toLowerCase() + "_" +
                request.getManufacturer().toLowerCase().replaceAll("\\s+", "") + "_" +
                request.getName().toLowerCase().replaceAll("\\s+", "-");

        // DTO를 Entity로 변환
        BaseSpec baseSpec = request.toEntity(generatedId);

        // BaseSpec을 저장합니다.
        // 연관된 하위 스펙(CpuSpec 등)은 CascadeType.ALL 설정에 의해 함께 저장됩니다.
        BaseSpec savedBaseSpec = baseSpecRepository.save(baseSpec);

        return savedBaseSpec.getId();
    }

    public Page<BaseSpecListResponse> listBaseSpecs(BaseSpecSearchCondition condition, Pageable pageable) {
        Page<BaseSpec> baseSpecPage = baseSpecRepository.searchBaseSpecs(condition, pageable);
        return baseSpecPage.map(BaseSpecListResponse::new); // 엔티티를 DTO로 변환
    }

    public BaseSpecDetailResponse getBaseSpecDetail(String baseSpecId) {
        BaseSpec baseSpec = baseSpecRepository.findById(baseSpecId)
                .orElseThrow(() -> new EntityNotFoundException("BaseSpec not found with id: " + baseSpecId));
        return new BaseSpecDetailResponse(baseSpec);
    }
    
    @Transactional
    public void deleteBaseSpec(String baseSpecId) {
        // 1. BaseSpec 조회
        BaseSpec baseSpec = baseSpecRepository.findById(baseSpecId)
                .orElseThrow(() -> new EntityNotFoundException("BaseSpec not found with id: " + baseSpecId));

        // 2. BaseSpec 소프트 삭제
        baseSpec.softDelete();

        // 3. 연관된 Product들 소프트 삭제
        List<Product> productsToDelete = productRepository.findProductsByBaseSpecIds(List.of(baseSpecId));
        for (Product product : productsToDelete) {
            product.softDelete();
        }

        // 4. 연관된 CompatibilityScore 삭제
        compatibilityScoreRepository.deleteAllBySpecId(baseSpecId);

        // 5. 연관된 PopularityScore 삭제
        popularityScoreRepository.deleteAllBySpecId(baseSpecId);

    }

    @Transactional
    public void updateBaseSpec(String baseSpecId, BaseSpecUpdateRequest request) {
        BaseSpec baseSpec = baseSpecRepository.findById(baseSpecId)
                .orElseThrow(() -> new EntityNotFoundException("BaseSpec not found with id: " + baseSpecId));

        // 1. 기본 정보 업데이트
        baseSpec.updateBaseInfo(request.getName(), request.getManufacturer(), request.getImageUrl());

        // 2. 카테고리별 상세 스펙 업데이트
        switch (baseSpec.getCategory()) {
            case CPU:
                if (request.getCpuSpec() != null) {
                    CpuSpec cpuSpec = baseSpec.getCpuSpec();
                    if (cpuSpec != null) {
                        // List<String>을 콤마로 구분된 String으로 변환
                        String supportedMemoryTypesStr = String.join(",", request.getCpuSpec().getSupportedMemoryTypes());

                        cpuSpec.updateSpec(request.getCpuSpec().getSocket(), supportedMemoryTypesStr, request.getCpuSpec().getHasIgpu());
                    }
                }
                break;
            case Motherboard:
                if (request.getMotherboardSpec() != null) {
                    MotherboardSpec motherboardSpec = baseSpec.getMotherboardSpec();
                    if (motherboardSpec != null) {
                        motherboardSpec.updateSpec(request.getMotherboardSpec().getSocket(), request.getMotherboardSpec().getChipset(), request.getMotherboardSpec().getMemoryType(), request.getMotherboardSpec().getMemorySlots(), request.getMotherboardSpec().getFormFactor(), request.getMotherboardSpec().getPcieVersion(), request.getMotherboardSpec().getPcieLanes());
                    }
                }
                break;
            case RAM:
                if (request.getRamSpec() != null) {
                    RamSpec ramSpec = baseSpec.getRamSpec();
                    if (ramSpec != null) {
                        ramSpec.updateSpec(request.getRamSpec().getMemoryType(), request.getRamSpec().getSpeedMhz(), request.getRamSpec().getCapacityGb(), request.getRamSpec().getKitQuantity(), request.getRamSpec().getHeightMm());
                    }
                }
                break;
            case GPU:
                if (request.getGpuSpec() != null) {
                    GpuSpec gpuSpec = baseSpec.getGpuSpec();
                    if (gpuSpec != null) {
                        gpuSpec.updateSpec(request.getGpuSpec().getPcieVersion(), request.getGpuSpec().getPcieLanes(), request.getGpuSpec().getLengthMm());
                    }
                }
                break;
        }
        // baseSpecRepository.save(baseSpec)는 @Transactional에 의해 자동으로 처리됨 (Dirty Checking)
    }
}