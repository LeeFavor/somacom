package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.request.ProductCreateRequest;
import com.kosta.somacom.dto.response.BaseSpecSearchResponse;
import com.kosta.somacom.service.SellerProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/seller")
@RequiredArgsConstructor
public class SellerProductController {

    private final SellerProductService sellerProductService;

    /**
     * S-201: 기반 모델 검색 API
     */
    @GetMapping("/base-specs")
    public ResponseEntity<List<BaseSpecSearchResponse>> searchBaseSpecs(@RequestParam String query) {
        List<BaseSpecSearchResponse> results = sellerProductService.searchBaseSpecs(query);
        return ResponseEntity.ok(results);
    }

    /**
     * S-201.3: 판매 상품 등록 API
     */
    @PostMapping("/products")
    public ResponseEntity<String> createProduct(@Valid @RequestBody ProductCreateRequest request,
                                                @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        Long newProductId = sellerProductService.createProduct(request, sellerId);
        URI location = URI.create("/api/products/" + newProductId);

        return ResponseEntity.created(location).body("Product created successfully with ID: " + newProductId);
    }
}