package com.kosta.somacom.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.AutocompleteResponse;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.service.ProductSearchService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSimpleResponse>> searchProducts(
            @RequestParam Map<String, String> allParams, Pageable pageable,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        System.out.println("===== Received Search Parameters =====");
        allParams.forEach((key, value) -> System.out.println(key + " : " + value));
        System.out.println("======================================");


        ProductSearchCondition condition = new ProductSearchCondition();
        Long userId = (principalDetails != null) ? principalDetails.getUser().getId() : null;
        condition.setUserId(userId);

        // 기본 파라미터 설정
        condition.setKeyword(allParams.get("keyword"));
        condition.setCategory(allParams.get("category"));
        condition.setCompatFilter(Boolean.parseBoolean(allParams.get("compatFilter")));

        // 나머지 파라미터를 동적 필터로 설정
        Map<String, String> filters = new HashMap<>();
        allParams.forEach((key, value) -> {
            if (!key.equals("keyword") && !key.equals("category") && !key.equals("compatFilter") &&
                !key.equals("page") && !key.equals("size") && !key.equals("sort")) {
                if (StringUtils.hasText(value)) {
                    filters.put(key, value);
                }
            }
        });
        condition.setFilters(filters);

        Page<ProductSimpleResponse> results = productSearchService.searchProducts(condition, pageable, userId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<List<AutocompleteResponse>> getAutocompleteSuggestions(@RequestParam String keyword) {
        if (!StringUtils.hasText(keyword) || keyword.trim().length() < 2) {
            // 너무 짧은 검색어는 무시
            return ResponseEntity.ok(Collections.emptyList());
        }
        List<AutocompleteResponse> suggestions = productSearchService.getAutocompleteSuggestions(keyword);
        return ResponseEntity.ok(suggestions);
    }
}