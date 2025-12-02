package com.kosta.somacom.domain.part;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "base_specs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseSpec {

    @Id
    @Column(name = "base_spec_id", length = 100)
    private String id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(nullable = false, length = 100)
    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PartCategory category;

    @Column(name = "image_url")
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 양방향 OneToOne 관계 설정
    @OneToOne(mappedBy = "baseSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private CpuSpec cpuSpec;

    @OneToOne(mappedBy = "baseSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private MotherboardSpec motherboardSpec;

    @OneToOne(mappedBy = "baseSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private RamSpec ramSpec;

    @OneToOne(mappedBy = "baseSpec", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private GpuSpec gpuSpec;

    @Builder
    public BaseSpec(String id, String name, String manufacturer, PartCategory category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.manufacturer = manufacturer;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    // 연관관계 편의 메소드
    public void setCpuSpec(CpuSpec cpuSpec) {
        if (this.cpuSpec != null) {
            this.cpuSpec.setBaseSpec(null);
        }
        this.cpuSpec = cpuSpec;
        if (cpuSpec != null) {
            cpuSpec.setBaseSpec(this);
        }
    }

    public void setMotherboardSpec(MotherboardSpec motherboardSpec) {
        if (this.motherboardSpec != null) {
            this.motherboardSpec.setBaseSpec(null);
        }
        this.motherboardSpec = motherboardSpec;
        if (motherboardSpec != null) {
            motherboardSpec.setBaseSpec(this);
        }
    }

    public void setRamSpec(RamSpec ramSpec) {
        if (this.ramSpec != null) {
            this.ramSpec.setBaseSpec(null);
        }
        this.ramSpec = ramSpec;
        if (ramSpec != null) {
            ramSpec.setBaseSpec(this);
        }
    }

    public void setGpuSpec(GpuSpec gpuSpec) {
        if (this.gpuSpec != null) {
            this.gpuSpec.setBaseSpec(null);
        }
        this.gpuSpec = gpuSpec;
        if (gpuSpec != null) {
            gpuSpec.setBaseSpec(this);
        }
    }

    public void updateBaseInfo(String name, String manufacturer, String imageUrl) {
        if (StringUtils.hasText(name)) {
            this.name = name;
        }
        if (StringUtils.hasText(manufacturer)) {
            this.manufacturer = manufacturer;
        }
        if (StringUtils.hasText(imageUrl)) {
            this.imageUrl = imageUrl;
        }
    }
}