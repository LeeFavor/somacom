package com.kosta.somacom.domain.part;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cpu_specs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CpuSpec {

    @Id
    @Column(name = "base_spec_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // baseSpec 필드를 id 필드에 매핑
    @JoinColumn(name = "base_spec_id")
    private BaseSpec baseSpec;

    @Column(nullable = false, length = 100)
    private String socket;

    @Column(name = "supported_memory_types")
    private String supportedMemoryTypes;

    @Column(name = "has_igpu")
    private boolean hasIgpu = false;

    @Builder
    public CpuSpec(BaseSpec baseSpec, String socket, String supportedMemoryTypes, boolean hasIgpu) {
        this.baseSpec = baseSpec;
        this.socket = socket;
        this.supportedMemoryTypes = supportedMemoryTypes;
        this.hasIgpu = hasIgpu;
    }
    
    // 연관관계 편의 메소드
    public void setBaseSpec(BaseSpec baseSpec) {
        this.baseSpec = baseSpec;
    }
}