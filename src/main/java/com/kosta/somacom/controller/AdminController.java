package com.kosta.somacom.controller;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.admin.dto.UserManagementResponse;
import com.kosta.somacom.admin.dto.UserStatusUpdateRequest;
import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.request.BaseSpecRequestProcessDto;
import com.kosta.somacom.dto.response.BaseSpecRequestResponseDto;
import com.kosta.somacom.order.dto.OrderListResponseDto;
import com.kosta.somacom.service.AdminService;
import com.kosta.somacom.service.LogService;
import com.kosta.somacom.service.OrderService;
import com.kosta.somacom.service.ProductImageBatchService;
import com.kosta.somacom.service.RecommendationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RecommendationService recommendationService;
    private final JobLauncher jobLauncher;
    private final Job compatibilityBatchJob;
    private final Job popularityBatchJob;
    private final OrderService orderService;
    private final LogService logService;
    private final ProductImageBatchService productImageBatchService;

    @GetMapping("/seller-requests")
    public ResponseEntity<Page<SellerRequestDto>> getSellerRequests(Pageable pageable) {
        Page<SellerRequestDto> requests = adminService.getSellerRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/seller-requests/{userId}/approve")
    public ResponseEntity<Void> approveSellerRequest(@PathVariable Long userId) {
        adminService.approveSellerRequest(userId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/seller-requests/{userId}/suspended")
    public ResponseEntity<Void> suspendSellerRequest(@PathVariable Long userId) {
        adminService.suspendSellerRequest(userId);
        return ResponseEntity.ok().build();
    }
    /**
     * A-203: PENDING 상태의 모델 등록 요청 목록 조회 API
     */
    @GetMapping("/base-spec-requests")
    public ResponseEntity<Page<BaseSpecRequestResponseDto>> getPendingBaseSpecRequests(Pageable pageable) {
        Page<BaseSpecRequestResponseDto> requests = adminService.getPendingBaseSpecRequests(pageable);
        return ResponseEntity.ok(requests);
    }

    /**
     * A-203: 모델 등록 요청 처리 API
     */
    @PutMapping("/base-spec-requests/{requestId}")
    public ResponseEntity<Void> processBaseSpecRequest(
            @PathVariable Long requestId,
            @Valid @RequestBody BaseSpecRequestProcessDto requestDto) {
        adminService.processBaseSpecRequest(requestId, requestDto);
        return ResponseEntity.ok().build();
    }

    /**
     * A-102: 회원/판매자 목록 조회 API
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserManagementResponse>> getAllUsers(@RequestParam(value = "keyword", required = false) String keyword, Pageable pageable) {
        Page<UserManagementResponse> users = adminService.getAllUsers(keyword, pageable);
        return ResponseEntity.ok(users);
    }
    /**
     * A-101: 회원 주문 수 조회 API
     */
    @GetMapping("/ordercounts")
    public ResponseEntity<Long> getOrders(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        Long orders = orderService.findOrdersCounter();
        return ResponseEntity.ok(orders);
    }
    
    /**
     * A-101: 회원 주문 수 조회 API
     */
    @GetMapping("/usercounts")
    public ResponseEntity<Long> getUserCounts(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        
        Long orders = adminService.findUsersCounts();
        return ResponseEntity.ok(orders);
    }

    /**
     * A-102: 회원/판매자 계정 상태 변경 API
     */
    @PutMapping("/users/{userId}/status")
    public ResponseEntity<Void> updateUserStatus(
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusUpdateRequest request) {
        adminService.updateUserStatus(userId, request.getStatus());
        return ResponseEntity.ok().build();
    }

    /**
     * A-401: DB의 모든 BaseSpec을 Google Cloud Retail 카탈로그에 동기화합니다.
     */
    @PostMapping("/sync/catalog")
    public ResponseEntity<String> syncCatalog() {
        try {
            String result = recommendationService.syncCatalogFromDb();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Catalog sync failed: " + e.getMessage());
        }
    }

    /**
     * A-402: 호환성 점수 계산 배치를 수동으로 실행합니다.
     */
    @PostMapping("/batch/compatibility")
    public ResponseEntity<String> runCompatibilityJob() {
        log.info("Manual trigger for compatibility batch job...");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매번 새로운 Job 인스턴스를 생성하기 위함
                    .toJobParameters();
            jobLauncher.run(compatibilityBatchJob, jobParameters);
            return ResponseEntity.ok("Compatibility batch job started successfully.");
        } catch (Exception e) {
            log.error("Failed to run compatibility batch job manually", e);
            return ResponseEntity.internalServerError().body("Failed to start compatibility batch job: " + e.getMessage());
        }
    }

    /**
     * A-403: 인기도 점수 계산 배치를 수동으로 실행합니다.
     */
    @PostMapping("/batch/popularity")
    public ResponseEntity<String> runPopularityJob() {
        log.info("Manual trigger for popularity batch job...");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .toJobParameters();
            jobLauncher.run(popularityBatchJob, jobParameters);
            return ResponseEntity.ok("Popularity batch job started successfully.");
        } catch (Exception e) {
            log.error("Failed to run popularity batch job manually", e);
            return ResponseEntity.internalServerError().body("Failed to start popularity batch job: " + e.getMessage());
        }
    }

    /**
     * [신규] 제품 이미지 자동 업데이트 배치를 수동으로 실행합니다.
     */
    @PostMapping("/batch/product-images")
    public ResponseEntity<String> runProductImageBatch() {
        productImageBatchService.runBatchUpdate();
        return ResponseEntity.ok("Product image update batch job has been triggered asynchronously.");
    }

    /**
     * [신규] 제품 이미지 다양화 배치를 수동으로 실행합니다. (모델별 20개 수집 후 분산 배정)
     */
    @PostMapping("/batch/product-images/diverse")
    public ResponseEntity<String> runDiverseProductImageBatch() {
        productImageBatchService.runDiverseImageBatchUpdate();
        return ResponseEntity.ok("Diverse product image update batch job has been triggered asynchronously.");
    }
    
    /**
     * 시스템 로그를 조회하는 API
     * @return 최신 로그 텍스트
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getSystemLogs() {
        String logs = logService.getLatestLogs();
        return ResponseEntity.ok(logs);
    }
}