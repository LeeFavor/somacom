package com.kosta.somacom.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.dto.request.BaseSpecRequestProcessDto;
import com.kosta.somacom.dto.response.BaseSpecRequestResponseDto;
import com.kosta.somacom.service.AdminService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

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
}