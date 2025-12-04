package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.dto.response.RecommendationResponseDto;
import com.kosta.somacom.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/personal")
    public ResponseEntity<List<RecommendationResponseDto>> getPersonalRecommendations(
            @AuthenticationPrincipal PrincipalDetails principalDetails,
            @RequestParam(defaultValue = "detail-page-view") String eventType,
            @RequestParam(defaultValue = "5") int count) throws IOException {

        String userId = String.valueOf(principalDetails.getUser().getId());
        List<RecommendationResponseDto> recommendations = recommendationService.getRecommendations(userId, eventType, count);
        return ResponseEntity.ok(recommendations);
    }
}