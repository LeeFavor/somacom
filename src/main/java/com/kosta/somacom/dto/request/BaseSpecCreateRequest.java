package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.part.RamSpec;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
public class BaseSpecCreateRequest {

    @NotBlank(message = "기반 모델명은 필수입니다.")
    private String name;

    @NotBlank(message = "제조사는 필수입니다.")
    private String manufacturer;

    @NotNull(message = "모델 타입은 필수입니다.")
    private PartCategory category;

    private String imageUrl;

    // 각 카테고리별 상세 스펙 DTO
    @Valid
    private CpuSpecDto cpuSpec;

    @Valid
    private MotherboardSpecDto motherboardSpec;

    @Valid
    private RamSpecDto ramSpec;

    @Valid
    private GpuSpecDto gpuSpec;

    /**
     * DTO를 BaseSpec 엔티티로 변환하는 메소드
     * @return BaseSpec 엔티티
     */
    public BaseSpec toEntity(String generatedId) {
        BaseSpec baseSpecEntity = BaseSpec.builder()
                .id(generatedId)
                .name(this.name)
                .manufacturer(this.manufacturer)
                .category(this.category)
                .imageUrl(this.imageUrl)
                .build();

        // 카테고리에 맞는 상세 스펙 엔티티를 생성하고 연관관계를 설정
        switch (this.category) {
            case CPU:
                if (this.cpuSpec != null) {
                    CpuSpec cpuSpecEntity = this.cpuSpec.toEntity(baseSpecEntity);
                    baseSpecEntity.setCpuSpec(cpuSpecEntity);
                }
                break;
            case Motherboard:
                if (this.motherboardSpec != null) {
                    MotherboardSpec motherboardSpecEntity = this.motherboardSpec.toEntity(baseSpecEntity);
                    baseSpecEntity.setMotherboardSpec(motherboardSpecEntity);
                }
                break;
            case RAM:
                if (this.ramSpec != null) {
                    RamSpec ramSpecEntity = this.ramSpec.toEntity(baseSpecEntity);
                    baseSpecEntity.setRamSpec(ramSpecEntity);
                }
                break;
            case GPU:
                if (this.gpuSpec != null) {
                    GpuSpec gpuSpecEntity = this.gpuSpec.toEntity(baseSpecEntity);
                    baseSpecEntity.setGpuSpec(gpuSpecEntity);
                }
                break;
            // 다른 카테고리 케이스 추가
            default:
                // 처리할 스펙이 없는 경우
                break;
        }

        return baseSpecEntity;
    }
}