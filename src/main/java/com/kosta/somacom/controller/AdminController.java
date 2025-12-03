package com.kosta.somacom.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.admin.dto.UserManagementResponse;
import com.kosta.somacom.admin.dto.UserStatusUpdateRequest;
import com.kosta.somacom.dto.request.BaseSpecRequestProcessDto;
import com.kosta.somacom.dto.response.BaseSpecRequestResponseDto;
import com.kosta.somacom.service.AdminService;
import com.kosta.somacom.service.RecommendationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final RecommendationService recommendationService;

    @GetMapping("/seller-requests")
    public ResponseEntity<List<SellerRequestDto>> getSellerRequests() {
        List<SellerRequestDto> requests = adminService.getSellerRequests();
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/seller-requests/{userId}/approve")
    public ResponseEntity<Void> approveSellerRequest(@PathVariable Long userId) {
        adminService.approveSellerRequest(userId);
        return ResponseEntity.ok().build();
    }
    /**
     * A-203: PENDING 상태의 모델 등록 요청 목록 조회 API
     */
    @GetMapping("/base-spec-requests")
    public ResponseEntity<List<BaseSpecRequestResponseDto>> getPendingBaseSpecRequests() {
        List<BaseSpecRequestResponseDto> requests = adminService.getPendingBaseSpecRequests();
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
    public ResponseEntity<List<UserManagementResponse>> getAllUsers() {
        List<UserManagementResponse> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
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
}