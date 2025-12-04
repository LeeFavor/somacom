package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.response.RecommendationResponseDto;
import com.kosta.somacom.service.RecommendationTestService;
import com.kosta.somacom.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;
    private final RecommendationTestService recommendationTestService; // 테스트용 서비스 주입

    @GetMapping("/personal")
    public ResponseEntity<List<RecommendationResponseDto>> getPersonalRecommendations(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(defaultValue = "detail-page-view") String eventType,
            @RequestParam(defaultValue = "5") int count) throws IOException {

        String userId = String.valueOf(principalDetails.getUser().getId());
        List<RecommendationResponseDto> recommendations = recommendationService.getRecommendations(userId, eventType, count);
        return ResponseEntity.ok(recommendations);
    }

    /**
     * [신규] 모델 학습을 위한 사용자 이벤트 대량 전송 테스트 API
     */
    @PostMapping("/ingest-events")
    public ResponseEntity<String> ingestEvents(@RequestBody Map<String, List<String>> payload) {
        String userId = "test-user-001"; // 테스트용 고정 사용자 ID
        List<String> productIds = payload.get("productIds");
        String result = recommendationTestService.ingestMultipleDetailViewEvents(userId, productIds);
        return ResponseEntity.ok(result);
    }
}