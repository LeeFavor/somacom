package com.kosta.somacom.dto.request;

import lombok.Data;

import java.util.Map;

@Data
public class ProductSearchCondition {
    private String keyword; // 상품명, 모델명 등 키워드 검색
    private String category; // CPU, MBD, RAM, GPU 등

    // 상세 필터 (예: "socket" -> "LGA1700", "manufacturer" -> "Intel")
    private Map<String, String> filters;
}