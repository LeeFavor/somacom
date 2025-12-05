package com.kosta.somacom.controller;

import com.google.api.gax.rpc.ApiException;
import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.response.RecommendationResponseDto;
import com.kosta.somacom.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personal")
    public ResponseEntity<List<RecommendationResponseDto>> getPersonalRecommendations(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(defaultValue = "detail-page-view") String eventType,
            @RequestParam(defaultValue = "5") int count) {

        try {
            String userId = String.valueOf(principalDetails.getUser().getId());
            List<RecommendationResponseDto> recommendations = recommendationService.getRecommendations(userId, eventType, count);
            return ResponseEntity.ok(recommendations);
        } catch (ApiException | IOException e) {
            // Google Cloud API 호출 실패 또는 기타 I/O 오류 발생 시
            // 500 오류 대신 200 OK와 함께 비어 있는 목록을 반환하여 프론트엔드 처리를 단순화합니다.
            log.error("Failed to get recommendations for user {}: {}", principalDetails.getUser().getId(), e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}