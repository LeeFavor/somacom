package com.kosta.somacom.dto.request;

import com.kosta.somacom.domain.part.PartCategory;
import lombok.Data;

@Data
public class BaseSpecSearchCondition {
    private String query;
    private PartCategory category;
}