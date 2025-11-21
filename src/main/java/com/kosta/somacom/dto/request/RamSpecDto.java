package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.RamSpec;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class RamSpecDto {

    @NotBlank(message = "RAM 메모리 타입은 필수입니다.")
    private String memoryType;

    @NotNull(message = "동작 속도는 필수입니다.")
    private int speedMhz;

    @NotNull(message = "모듈 1개당 용량은 필수입니다.")
    private int capacityGb;

    @NotNull(message = "킷 수량은 필수입니다.")
    @Min(value = 1, message = "킷 수량은 1개 이상이어야 합니다.")
    private int kitQuantity = 1;

    private Integer heightMm;

    public RamSpec toEntity(BaseSpec baseSpec) {
        return RamSpec.builder()
                .baseSpec(baseSpec)
                .memoryType(this.memoryType)
                .speedMhz(this.speedMhz)
                .capacityGb(this.capacityGb)
                .kitQuantity(this.kitQuantity)
                .heightMm(this.heightMm)
                .build();
    }
}