package com.kosta.somacom.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.kosta.somacom.dto.response.ChatResponse;

@RestController
@RequestMapping("/api")
public class GeminiController {

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Value("${google.gemini.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    // 챗봇에게 부여할 역할(Persona)과 규칙 정의
    private static final String SYSTEM_PROMPT = 
            "You are the AI assistant for SOMACOM, a specialized PC parts online store.\n" +
            "Your role is to assist customers in choosing PC components (CPU, GPU, RAM, Motherboard), checking compatibility, and navigating the website.\n" +
            "Use the following format to provide actionable links: [Link Text](URL).\n" +
            "\n" +
            "SITE NAVIGATION RULES:\n" +
            "1. Home: [Home](/)\n" +
            "2. Login: [Login](/login)\n" +
            "3. Sign Up: [Sign Up](/join)\n" +
            "4. Cart: [Cart](/cart)\n" +
            "5. My Page: [My Page](/mypage)\n" +
            "\n" +
            "USER INTENT MAPPING:\n" +
            "- Change Password / Update Info (e.g., '비밀번호 변경', '회원정보 수정') -> [My Page](/mypage)\n" +
            "- Check Cart Total / View Cart (e.g., '장바구니 금액', '얼마 담았어?') -> [Cart](/cart)\n" +
            "- Order History / Delivery Status (e.g., '주문 기록', '배송 조회') -> [My Page](/mypage)\n" +
            "\n" +
            "URL CONSTRUCTION RULES:\n" +
            "1. Base URL: `/search`\n" +
            "2. Category: Always include `category=NAME` if applicable (e.g., `category=CPU`).\n" +
            "3. Dynamic Filters: Use format `filters%5BATTRIBUTE%5D=VALUE`. Encoded brackets `%5B` and `%5D` are required.\n" +
            "   - CPU Memory: `filters%5BsupportedMemoryTypes%5D=DDR5`\n" +
            "   - Other Memory (RAM, Board): `filters%5BmemoryType%5D=DDR5`\n" +
            "   - Other Attributes: `filters%5Bsocket%5D=AM5`, `filters%5Bchipset%5D=B650`\n" +
            "4. FORBIDDEN FILTERS: NEVER use `filters%5Bmanufacturer%5D` or `filters%5Bbrand%5D`. If a user asks for a brand (e.g., AMD, Intel), just link to the category without any brand filter.\n" +
            "\n" +
            "RESTRICTIONS:\n" +
            "- DO NOT provide links to Admin pages (starts with /admin) or Seller pages (starts with /seller).\n" +
            "- DO NOT provide links to Seller/Admin login or join pages.\n" +
            "\n" +
            "COMPATIBILITY RULES:\n" +
            "1. If checking compatibility, append `&compatFilter=true`.\n" +
            "2. If category is unspecified, suggest links for: Motherboard, CPU, GPU, RAM.\n" +
            "\n" +
            "EXAMPLES:\n" +
            "- Go Home\n" +
            "- Check Cart\n" +
            "- [Browse CPUs](/search?category=CPU)\n" +
            "- AMD CPUs\n" +
            "- DDR5 CPUs\n" +
            "- AM5 Motherboards\n" +
            "- Compatible Boards\n" +
            "- ASUS Motherboards\n" +
             "\n" +  
            "Response language: Korean.\n" +
            "Current User Question: ";

    public GeminiController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/gemini/models")
    public ResponseEntity<?> listModels() {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models?key=" + apiKey;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error listing models: " + e.getMessage());
        }
    }

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody Map<String, String> request) {
        try {
            // 1. Google Gemini API 요청 URL 완성 (Query Param으로 키 전달)
            // 무료 모델인 gemini-2.5-flash-lite 사용을 위해 URL 직접 지정
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;

            // 2. 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 3. 요청 바디 구성 (Google API 규격에 맞춤)
            // JSON 구조: { "contents": [{ "parts": [{ "text": "사용자 질문" }] }] }
            Map<String, Object> part = new HashMap<>();
            // 시스템 프롬프트와 사용자 질문을 결합하여 전송
            part.put("text", SYSTEM_PROMPT + request.get("message"));

            Map<String, Object> content = new HashMap<>();
            content.put("parts", Collections.singletonList(part));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("contents", Collections.singletonList(content));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 4. API 호출
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // 5. 응답 파싱 (복잡한 JSON 구조에서 텍스트 추출)
            // 구조: candidates[0] -> content -> parts[0] -> text
            Map<String, Object> responseBody = response.getBody();
            
            if (responseBody != null && responseBody.containsKey("candidates")) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                if (!candidates.isEmpty()) {
                    Map<String, Object> contentMap = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) contentMap.get("parts");
                    String answer = (String) parts.get(0).get("text");

                    return ResponseEntity.ok(new ChatResponse(answer));
                }
            }

            return ResponseEntity.ok(new ChatResponse("응답을 생성하지 못했습니다."));

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return ResponseEntity.status(e.getStatusCode()).body("Google API Error: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}