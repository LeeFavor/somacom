package com.kosta.somacom.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.service.ProductSearchService;

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
        Page<ProductSimpleResponse> results = productSearchService.searchProducts(condition, pageable, userId);
        return ResponseEntity.ok(results);
    }
}