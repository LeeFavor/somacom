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

    @Builder
    public MotherboardSpec(BaseSpec baseSpec, String socket, String chipset, String memoryType, int memorySlots, String formFactor) {
        this.baseSpec = baseSpec;
        this.socket = socket;
        this.chipset = chipset;
        this.memoryType = memoryType;
        this.memorySlots = memorySlots;
        this.formFactor = formFactor;
    }
    
    // 연관관계 편의 메소드
    public void setBaseSpec(BaseSpec baseSpec) {
        this.baseSpec = baseSpec;
    }
}