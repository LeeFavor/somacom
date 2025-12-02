package com.kosta.somacom.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class BaseSpecUpdateRequest {

    @NotBlank(message = "기반 모델명은 필수입니다.")
    private String name;

    @NotBlank(message = "제조사는 필수입니다.")
    private String manufacturer;

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
}