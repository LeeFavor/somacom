package com.kosta.somacom.dto.response;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BaseSpecSearchResponse {
    private String id;
    private String name;
    private String manufacturer;
    private PartCategory category;

    public BaseSpecSearchResponse(BaseSpec baseSpec) {
        this.id = baseSpec.getId();
        this.name = baseSpec.getName();
        this.manufacturer = baseSpec.getManufacturer();
        this.category = baseSpec.getCategory();
    }
}