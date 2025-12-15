package com.kosta.somacom.controller;

import com.kosta.somacom.service.RecommendationTestService;
import com.google.cloud.retail.v2.PredictResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors; // Collectors 임포트

/**
 * AI 추천 엔진 3단계 테스트를 위한 API 엔드포인트
 */
@RestController
public class RecommendationTestController {

    @Autowired
    private RecommendationTestService recommendationService;

    /**
     * [1단계] 카탈로그 저장 (101개 항목)
     */
    @GetMapping("/test/1-import-catalog")
    public String testImportCatalog() {
        try {
            // v3.14: 101개 (11개 실제 + 90개 더미) 카탈로그 전송
            return recommendationService.importCatalog();
        } catch (Exception e) {
            e.printStackTrace();
            return "Error importing catalog: " + e.getMessage();
        }
    }

    /**
     * [2단계] 사용자 로그 저장 (표준 eventType 사용)
     */
    @GetMapping("/test/2-ingest-logs")
    public String testIngestLogs() {
        // ai_simulation_report.md의 로그 6건 순차적 전송
        String userId = "user_001";
        try {
            // [수정 v3.12] eventContext 변수 사용
            recommendationService.ingestUserEvent(userId, "search", "i7 14700k"); // 상품 무관
            recommendationService.ingestUserEvent(userId, "detail-page-view", "base_14700k"); // 상품 연관
            recommendationService.ingestUserEvent(userId, "detail-page-view", "base_z790_d5"); // 상품 연관
            recommendationService.ingestUserEvent(userId, "detail-page-view", "base_b760_d4"); // 상품 연관
            recommendationService.ingestUserEvent(userId, "add-to-cart", "base_14700k"); // 상품 연관
            recommendationService.ingestUserEvent(userId, "add-to-cart", "base_z790_d5"); // 상품 연관
            
            return "Successfully ingested 6 events for user_001.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error ingesting events: " + e.getMessage();
        }
    }

    /**
     * [3단계-A] 추천 요청 ("자주 함께 구매하는 항목" 모델)
     */
    @GetMapping("/test/3-get-recommendations")
    public List<String> testGetRecommendations() {
        try {
            // 시뮬레이션 [4단계]의 컨텍스트
            String userId = "user_001";
            List<String> cartItems = Arrays.asList("base_14700k", "base_z790_d5");

            List<PredictResponse.PredictionResult> results = 
                recommendationService.getRecommendations(userId, cartItems);

            // 클라이언트에는 base_spec_id만 반환 (시뮬레이션 [5단계]와 동일)
            AtomicInteger rank = new AtomicInteger(1);
            return results.stream()
                .map(result -> "Rank " + rank.getAndIncrement() + ": " + result.getId())
                .collect(Collectors.toList()); // [수정] Java 11 호환

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error getting recommendations (FBT): " + e.getMessage());
        }
    }

     /**
     * [3단계-B] 추천 요청 ("유사 품목" 모델 테스트용)
     */
    @GetMapping("/test/3-get-similar-items")
    public List<String> testGetSimilarItems() {
        try {
            // "i7-14700k"와 유사한 CPU 2개를 요청
            String referenceProductId = "base_ram_crucial_pro-48gb-(2x24gb)-ddr5-5600";

            List<PredictResponse.PredictionResult> results = 
                recommendationService.getSimilarItems(referenceProductId);

            AtomicInteger rank = new AtomicInteger(1);
            return results.stream()
                .map(result -> "Rank " + rank.getAndIncrement() + ": " + result.getId())
                .collect(Collectors.toList()); // [수정] Java 11 호환

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error getting recommendations (Similar Items): " + e.getMessage());
        }
    }
}