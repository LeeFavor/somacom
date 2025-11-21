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
@Table(name = "ram_specs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RamSpec {

    @Id
    @Column(name = "base_spec_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "base_spec_id")
    private BaseSpec baseSpec;

    @Column(name = "memory_type", nullable = false, length = 50)
    private String memoryType;

    @Column(name = "speed_mhz", nullable = false)
    private int speedMhz;

    @Column(name = "capacity_gb", nullable = false)
    private int capacityGb;

    @Column(name = "kit_quantity", nullable = false)
    private int kitQuantity = 1;

    @Column(name = "height_mm")
    private Integer heightMm;

    @Builder
    public RamSpec(BaseSpec baseSpec, String memoryType, int speedMhz, int capacityGb, int kitQuantity, Integer heightMm) {
        this.baseSpec = baseSpec;
        this.memoryType = memoryType;
        this.speedMhz = speedMhz;
        this.capacityGb = capacityGb;
        this.kitQuantity = kitQuantity;
        this.heightMm = heightMm;
    }
    
    // 연관관계 편의 메소드
    public void setBaseSpec(BaseSpec baseSpec) {
        this.baseSpec = baseSpec;
    }
}