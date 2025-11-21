package com.kosta.somacom.controller;

import com.kosta.somacom.dto.request.BaseSpecCreateRequest;
import com.kosta.somacom.service.AdminPartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/admin/parts")
@RequiredArgsConstructor
public class AdminPartController {

    private final AdminPartService adminPartService;

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
}