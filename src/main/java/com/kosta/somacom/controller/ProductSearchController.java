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
import java.util.List;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping("/search")
    public ResponseEntity<Page<ProductSimpleResponse>> searchProducts(
            @ModelAttribute ProductSearchCondition condition, Pageable pageable,
            @AuthenticationPrincipal PrincipalDetails principalDetails) {

        Long userId = (principalDetails != null) ? principalDetails.getUser().getId() : null;
        condition.setUserId(userId);

        // @ModelAttribute가 요청 파라미터를 condition 객체에 자동으로 바인딩해줍니다.

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