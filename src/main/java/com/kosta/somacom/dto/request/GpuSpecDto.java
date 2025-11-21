package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
public class GpuSpecDto {

    @NotNull(message = "PCIe 버전은 필수입니다.")
    private BigDecimal pcieVersion;

    @NotNull(message = "PCIe 레인은 필수입니다.")
    private int pcieLanes;

    private Integer lengthMm;

    public GpuSpec toEntity(BaseSpec baseSpec) {
        return GpuSpec.builder()
                .baseSpec(baseSpec)
                .pcieVersion(this.pcieVersion)
                .pcieLanes(this.pcieLanes)
                .lengthMm(this.lengthMm)
                .build();
    }
}