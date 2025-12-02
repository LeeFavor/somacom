package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CpuSpecDto {

    @NotBlank(message = "CPU 소켓 정보는 필수입니다.")
    private String socket;

    private List<String> supportedMemoryTypes;

    private Boolean hasIgpu;

    // 엔티티를 DTO로 변환하는 생성자
    public CpuSpecDto(CpuSpec entity) {
        this.socket = entity.getSocket();
        if (entity.getSupportedMemoryTypes() != null && !entity.getSupportedMemoryTypes().isEmpty()) {
            this.supportedMemoryTypes = Arrays.asList(entity.getSupportedMemoryTypes().split(","));
        }
        this.hasIgpu = entity.isHasIgpu();
    }

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