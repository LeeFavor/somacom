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

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "gpu_specs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GpuSpec {

    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getPcieVersion() {
		return pcieVersion;
	}

	public void setPcieVersion(BigDecimal pcieVersion) {
		this.pcieVersion = pcieVersion;
	}

	public Integer getPcieLanes() {
		return pcieLanes;
	}

	public void setPcieLanes(Integer pcieLanes) {
		this.pcieLanes = pcieLanes;
	}

	public Integer getLengthMm() {
		return lengthMm;
	}

	public void setLengthMm(Integer lengthMm) {
		this.lengthMm = lengthMm;
	}

	@Id
    @Column(name = "base_spec_id")
    private String id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "base_spec_id")
    private BaseSpec baseSpec;

    @Column(name = "pcie_version", nullable = false, precision = 3, scale = 1)
    private BigDecimal pcieVersion;

    @Column(name = "pcie_lanes", nullable = false)
    private Integer pcieLanes;

    @Column(name = "length_mm")
    private Integer lengthMm;

    @Builder
    public GpuSpec(BaseSpec baseSpec, BigDecimal pcieVersion, int pcieLanes, Integer lengthMm) {
        this.baseSpec = baseSpec;
        this.pcieVersion = pcieVersion;
        this.pcieLanes = pcieLanes;
        this.lengthMm = lengthMm;
    }
    
    // 연관관계 편의 메소드
    public void setBaseSpec(BaseSpec baseSpec) {
        this.baseSpec = baseSpec;
    }
    
    public void updateSpec(BigDecimal pcieVersion, Integer pcieLanes, Integer lengthMm) {
        if (pcieVersion != null) {
            this.pcieVersion = pcieVersion;
        }
        if (pcieLanes != null) {
            this.pcieLanes = pcieLanes;
        }
        if (lengthMm != null) {
            this.lengthMm = lengthMm;
        }
    }
}