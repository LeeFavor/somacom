package com.kosta.somacom.controller;

import com.kosta.somacom.dto.response.ProductDetailResponse;
import com.kosta.somacom.service.ProductDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductDetailController {

    private final ProductDetailService productDetailService;

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailResponse> getProductDetail(@PathVariable Long productId) {
        ProductDetailResponse productDetail = productDetailService.getProductDetail(productId);
        return ResponseEntity.ok(productDetail);
    }
}