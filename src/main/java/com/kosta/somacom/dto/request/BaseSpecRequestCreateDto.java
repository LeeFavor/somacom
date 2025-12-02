package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.PartCategory;
import lombok.Data;

@Data
public class BaseSpecRequestCreateDto {
    private String requestedModelName;
    private PartCategory category;
    private String manufacturer;
}