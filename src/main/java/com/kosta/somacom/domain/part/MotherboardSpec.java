package com.kosta.somacom.domain.part;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.util.StringUtils;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "motherboard_specs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MotherboardSpec {

    @Id
    @Column(name = "base_spec_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "base_spec_id")
    private BaseSpec baseSpec;

    @Column(nullable = false, length = 100)
    private String socket;

    @Column(nullable = false, length = 100)
    private String chipset;

    @Column(name = "memory_type", nullable = false, length = 50)
    private String memoryType;

    @Column(name = "memory_slots", nullable = false)
    private int memorySlots;

    @Column(name = "form_factor", length = 50)
    private String formFactor;

    @Column(name = "pcie_version", precision = 3, scale = 1)
    private BigDecimal pcieVersion;

    @Column(name = "pcie_lanes")
    private Integer pcieLanes;

    @Builder
    public MotherboardSpec(BaseSpec baseSpec, String socket, String chipset, String memoryType, int memorySlots, String formFactor, BigDecimal pcieVersion, Integer pcieLanes) {
        this.baseSpec = baseSpec;
        this.socket = socket;
        this.chipset = chipset;
        this.memoryType = memoryType;
        this.memorySlots = memorySlots;
        this.formFactor = formFactor;
        this.pcieVersion = pcieVersion;
        this.pcieLanes = pcieLanes;
    }
    
    // 연관관계 편의 메소드
    public void setBaseSpec(BaseSpec baseSpec) {
        this.baseSpec = baseSpec;
    }

    public void updateSpec(String socket, String chipset, String memoryType, Integer memorySlots, String formFactor, BigDecimal pcieVersion, Integer pcieLanes) {
        if (StringUtils.hasText(socket)) {
            this.socket = socket;
        }
        if (StringUtils.hasText(chipset)) {
            this.chipset = chipset;
        }
        if (StringUtils.hasText(memoryType)) {
            this.memoryType = memoryType;
        }
        if (memorySlots != null) {
            this.memorySlots = memorySlots;
        }
        if (StringUtils.hasText(formFactor)) {
            this.formFactor = formFactor;
        }
        if (pcieVersion != null) {
            this.pcieVersion = pcieVersion;
        }
        if (pcieLanes != null) {
            this.pcieLanes = pcieLanes;
        }
    }
}