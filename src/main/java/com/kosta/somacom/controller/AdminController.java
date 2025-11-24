package com.kosta.somacom.controller;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.service.AdminService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}