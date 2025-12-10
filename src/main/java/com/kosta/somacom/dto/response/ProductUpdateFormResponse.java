package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.product.ProductCondition;
import com.kosta.somacom.dto.request.CpuSpecDto;
import com.kosta.somacom.dto.request.GpuSpecDto;
import com.kosta.somacom.dto.request.MotherboardSpecDto;
import com.kosta.somacom.dto.request.RamSpecDto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ProductUpdateFormResponse {
    private Long productId;
    private String baseSpecName;
    private String name;
    private BigDecimal price;
    private Integer stockQuantity;
    private ProductCondition condition;
    private String description;
    private String imageUrl;
    
    // 기반 모델 정보
    private String baseSpecId;
    private String manufacturer;
    private PartCategory category;

    // 각 부품별 상세 스펙 정보
    private CpuSpecDto cpuSpec;
    private MotherboardSpecDto motherboardSpec;
    private RamSpecDto ramSpec;
    private GpuSpecDto gpuSpec;

    public ProductUpdateFormResponse(Product product) {
        this.productId = product.getId();
        this.baseSpecName = product.getBaseSpec().getName();
        this.name = product.getName();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.condition = product.getCondition();
        this.description = product.getDescription();
        this.imageUrl = product.getImage_url();
        
        BaseSpec baseSpec = product.getBaseSpec();
        this.baseSpecId = baseSpec.getId();
        this.baseSpecName = baseSpec.getName();
        this.manufacturer = baseSpec.getManufacturer();
        this.category = baseSpec.getCategory();
        
     // BaseSpec에 연결된 상세 스펙 정보를 DTO로 변환하여 할당
        switch (baseSpec.getCategory()) {
            case CPU:
                if (baseSpec.getCpuSpec() != null) {
                    this.cpuSpec = new CpuSpecDto(baseSpec.getCpuSpec());
                }
                break;
            case Motherboard:
                if (baseSpec.getMotherboardSpec() != null) {
                    this.motherboardSpec = new MotherboardSpecDto(baseSpec.getMotherboardSpec());
                }
                break;
            case RAM:
                if (baseSpec.getRamSpec() != null) {
                    this.ramSpec = new RamSpecDto(baseSpec.getRamSpec());
                }
                break;
            case GPU:
                if (baseSpec.getGpuSpec() != null) {
                    this.gpuSpec = new GpuSpecDto(baseSpec.getGpuSpec());
                }
                break;
            default:
                break;
        }
    }
    
    
}