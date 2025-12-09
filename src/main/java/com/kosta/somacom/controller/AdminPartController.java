package com.kosta.somacom.controller;

import com.kosta.somacom.dto.request.BaseSpecCreateRequest;
import com.kosta.somacom.dto.request.BaseSpecSearchCondition;
import com.kosta.somacom.dto.request.BaseSpecUpdateRequest;
import com.kosta.somacom.dto.response.BaseSpecDetailResponse;
import com.kosta.somacom.dto.response.BaseSpecListResponse;
import com.kosta.somacom.service.DataInitializationService;
import com.kosta.somacom.service.ProductDataGenerationService;
import com.kosta.somacom.service.AdminPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/admin/parts")
@RequiredArgsConstructor
public class AdminPartController {

    private final AdminPartService adminPartService;
    private final DataInitializationService dataInitializationService;
    private final ProductDataGenerationService productDataGenerationService;

    /**
     * A-201-ADD: 신규 기반 모델 등록
     */
    @PostMapping
    public ResponseEntity<String> createBaseSpec(@Valid @RequestBody BaseSpecCreateRequest request) {
        String newBaseSpecId = adminPartService.createBaseSpec(request);
        // 생성된 리소스의 URI를 Location 헤더에 담아 201 Created 응답 반환
        URI location = URI.create("/api/parts/" + newBaseSpecId);
        return ResponseEntity.created(location).body("Base spec created successfully with ID: " + newBaseSpecId);
    }

    /**
     * A-201-LIST: 기반 모델 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<BaseSpecListResponse>> listBaseSpecs(@ModelAttribute BaseSpecSearchCondition condition, Pageable pageable) {
        return ResponseEntity.ok(adminPartService.listBaseSpecs(condition, pageable));
    }

    /**
     * A-202: 기반 모델 상세 정보 조회 (수정 폼 채우기용)
     */
    @GetMapping("/{baseSpecId}")
    public ResponseEntity<BaseSpecDetailResponse> getBaseSpec(@PathVariable String baseSpecId) {
        BaseSpecDetailResponse response = adminPartService.getBaseSpecDetail(baseSpecId);
        return ResponseEntity.ok(response);
    }

    /**
     * A-202: 기반 모델 정보 수정
     */
    @PutMapping("/{baseSpecId}")
    public ResponseEntity<Void> updateBaseSpec(@PathVariable String baseSpecId, @Valid @RequestBody BaseSpecUpdateRequest request) {
        adminPartService.updateBaseSpec(baseSpecId, request);
        return ResponseEntity.ok().build();
    }
    
    /**
     * A-202: 기반 모델 정보 삭제
     */
    @DeleteMapping("/{baseSpecId}")
    public ResponseEntity<Void> deleteBaseSpec(@PathVariable String baseSpecId) {
        adminPartService.deleteBaseSpec(baseSpecId);
        return ResponseEntity.ok().build();
    }

    /**
     * [신규] basespec.txt 파일로부터 데이터를 DB에 초기화하는 API
     */
    @PostMapping("/initialize-from-file")
    public ResponseEntity<String> initializeDataFromFile() {
        dataInitializationService.initializeData();
        return ResponseEntity.ok("Data initialization from basespec.txt has been triggered.");
    }
    
    /**
     * [신규] 모든 BaseSpec에 대해 특정 판매자의 Product를 대량 생성하는 API
     */
    @PostMapping("/generate-products")
    public ResponseEntity<String> generateBulkProducts(
            @RequestParam Long sellerId,
            @RequestParam(defaultValue = "10") int count,
            @RequestParam String imageUrl) {

        // 비동기 실행을 원할 경우 @Async 어노테이션과 함께 별도 스레드에서 실행 고려
        productDataGenerationService.generateProductsForEachBaseSpec(sellerId, count, imageUrl);

        return ResponseEntity.ok("Bulk product generation has been triggered for sellerId: " + sellerId);
    }
}