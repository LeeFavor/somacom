package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class GpuSpecDto {

    @NotNull(message = "PCIe 버전은 필수입니다.")
    private BigDecimal pcieVersion;

    @NotNull(message = "PCIe 레인은 필수입니다.")
    private int pcieLanes;

    private Integer lengthMm;

    // 엔티티를 DTO로 변환하는 생성자
    public GpuSpecDto(GpuSpec entity) {
        this.pcieVersion = entity.getPcieVersion();
        this.pcieLanes = entity.getPcieLanes();
        this.lengthMm = entity.getLengthMm();
    }

    public GpuSpec toEntity(BaseSpec baseSpec) {
        return GpuSpec.builder()
                .baseSpec(baseSpec)
                .pcieVersion(this.pcieVersion)
                .pcieLanes(this.pcieLanes)
                .lengthMm(this.lengthMm)
                .build();
    }
}