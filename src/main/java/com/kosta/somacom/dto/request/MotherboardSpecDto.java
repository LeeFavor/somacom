package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import javax.validation.constraints.NotNull;

@Getter
@Setter
public class MotherboardSpecDto {

    @NotBlank(message = "메인보드 소켓 정보는 필수입니다.")
    private String socket;

    @NotBlank(message = "칩셋 정보는 필수입니다.")
    private String chipset;

    @NotBlank(message = "메모리 타입은 필수입니다.")
    private String memoryType;

    @NotNull(message = "메모리 슬롯 수는 필수입니다.")
    @Min(value = 1, message = "메모리 슬롯은 1개 이상이어야 합니다.")
    private int memorySlots;

    private String formFactor;

    private BigDecimal pcieVersion;
    private Integer pcieLanes;

    public MotherboardSpec toEntity(BaseSpec baseSpec) {
        return MotherboardSpec.builder()
                .baseSpec(baseSpec)
                .socket(this.socket)
                .chipset(this.chipset)
                .memoryType(this.memoryType)
                .memorySlots(this.memorySlots)
                .formFactor(this.formFactor)
                .pcieVersion(this.pcieVersion)
                .pcieLanes(this.pcieLanes)
                .build();
    }
}