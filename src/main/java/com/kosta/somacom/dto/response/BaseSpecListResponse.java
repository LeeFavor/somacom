package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseSpecListResponse {
    private String id;
    private String name;
    private PartCategory category;
    private String manufacturer;

 // 엔티티를 DTO로 변환하는 생성자
    public BaseSpecListResponse(BaseSpec entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.category = entity.getCategory();
        this.manufacturer = entity.getManufacturer();
    }
}
