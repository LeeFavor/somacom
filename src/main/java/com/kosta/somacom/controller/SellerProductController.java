package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.request.ProductCreateRequest;
import com.kosta.somacom.dto.request.BaseSpecRequestCreateDto;
import com.kosta.somacom.dto.request.ProductUpdateRequest;
import com.kosta.somacom.dto.response.BaseSpecSearchResponse;
import com.kosta.somacom.dto.response.SellerProductListResponse;
import com.kosta.somacom.dto.response.ProductUpdateFormResponse;
import com.kosta.somacom.service.SellerProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    /**
     * S-201.2: 신규 기반 모델 등록 요청 API
     */
    @PostMapping("/base-spec-requests")
    public ResponseEntity<String> requestNewBaseSpec(@Valid @RequestBody BaseSpecRequestCreateDto request,
                                                     @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        Long requestId = sellerProductService.requestNewBaseSpec(request, sellerId);
        URI location = URI.create("/api/seller/base-spec-requests/" + requestId);
        return ResponseEntity.created(location).body("Request created successfully with ID: " + requestId);
    }
    
    /**
     * S-202: 내 판매 상품 목록 조회 API
     */
    @GetMapping("/products")
    public ResponseEntity<Page<SellerProductListResponse>> getMyProducts(
            @AuthenticationPrincipal PrincipalDetails principalDetails, Pageable pageable) {
        Long sellerId = principalDetails.getUser().getId();
        Page<SellerProductListResponse> products = sellerProductService.getProductsBySeller(sellerId, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * S-203: 내 판매 상품 수정을 위한 정보 조회 API
     */
    @GetMapping("/products/{productId}/edit")
    public ResponseEntity<ProductUpdateFormResponse> getProductForUpdate(@PathVariable Long productId,
                                                                         @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        ProductUpdateFormResponse response = sellerProductService.getProductForUpdate(productId, sellerId);
        return ResponseEntity.ok(response);
    }

    /**
     * S-203: 내 판매 상품 수정 API
     */
    @PutMapping("/products/{productId}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long productId,
                                              @Valid @RequestBody ProductUpdateRequest request,
                                              @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        sellerProductService.updateProduct(productId, sellerId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * S-204: 내 판매 상품 삭제 API
     */
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId,
                                              @AuthenticationPrincipal PrincipalDetails principalDetails) {
        Long sellerId = principalDetails.getUser().getId();
        sellerProductService.deleteProduct(productId, sellerId);
        return ResponseEntity.noContent().build();
    }
}