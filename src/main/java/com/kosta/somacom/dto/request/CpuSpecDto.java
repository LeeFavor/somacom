package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class CpuSpecDto {

    @NotBlank(message = "CPU 소켓 정보는 필수입니다.")
    private String socket;

    private List<String> supportedMemoryTypes;

    private boolean hasIgpu;

    public CpuSpec toEntity(BaseSpec baseSpec) {
        return CpuSpec.builder()
                .baseSpec(baseSpec)
                .socket(this.socket)
                // List<String>을 콤마로 구분된 단일 문자열로 변환
                .supportedMemoryTypes(String.join(",", this.supportedMemoryTypes))
                .hasIgpu(this.hasIgpu)
                .build();
    }
}