package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.dto.request.CpuSpecDto;
import com.kosta.somacom.dto.request.GpuSpecDto;
import com.kosta.somacom.dto.request.MotherboardSpecDto;
import com.kosta.somacom.dto.request.RamSpecDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BaseSpecDetailResponse {

    private String id;
    private String name;
    private String manufacturer;
    private PartCategory category;
    private String imageUrl;

    // 각 카테고리별 상세 스펙 DTO
    private CpuSpecDto cpuSpec;
    private MotherboardSpecDto motherboardSpec;
    private RamSpecDto ramSpec;
    private GpuSpecDto gpuSpec;

    public BaseSpecDetailResponse(BaseSpec baseSpec) {
        this.id = baseSpec.getId();
        this.name = baseSpec.getName();
        this.manufacturer = baseSpec.getManufacturer();
        this.category = baseSpec.getCategory();
        this.imageUrl = baseSpec.getImageUrl();

        switch (baseSpec.getCategory()) {
            case CPU:
                if (baseSpec.getCpuSpec() != null) {
                    this.cpuSpec = new CpuSpecDto(baseSpec.getCpuSpec());
                }
                break;
            case Motherboard:
                if (baseSpec.getMotherboardSpec() != null) {
                    this.motherboardSpec = new MotherboardSpecDto(baseSpec.getMotherboardSpec());
                }
                break;
            case RAM:
                if (baseSpec.getRamSpec() != null) {
                    this.ramSpec = new RamSpecDto(baseSpec.getRamSpec());
                }
                break;
            case GPU:
                if (baseSpec.getGpuSpec() != null) {
                    this.gpuSpec = new GpuSpecDto(baseSpec.getGpuSpec());
                }
                break;
            default:
                break;
        }
    }
}