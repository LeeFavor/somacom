package com.kosta.somacom.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleImageSearchService {

    @Value("${google.api.key}")
    private String apiKey;

    @Value("${google.search.cx}")
    private String cx;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String GOOGLE_SEARCH_URL = "https://www.googleapis.com/customsearch/v1";

    /**
     * 구글 맞춤 검색 API를 사용하여 이미지 URL 목록을 검색합니다.
     * @param query 검색어
     * @return 이미지 URL 리스트 (검색 결과가 없으면 빈 리스트)
     */
    public List<String> searchImages(String query) {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_SEARCH_URL)
                    .queryParam("key", apiKey)
                    .queryParam("cx", cx)
                    .queryParam("q", query)
                    .queryParam("searchType", "image")
                    .queryParam("num", 10) // API 호출 1회당 최대 10개까지 가져올 수 있음 (비용 동일)
                    .queryParam("fileType", "jpg")
                    .build()
                    .toUri();

            ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
            
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("Google Search API failed for query: {}", query);
                return List.of();
            }

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode items = root.path("items");
            List<String> links = new ArrayList<>();

            if (items.isArray() && items.size() > 0) {
                for (JsonNode item : items) {
                    links.add(item.path("link").asText());
                }
                log.debug("Found {} images for query '{}'", links.size(), query);
            } else {
                log.warn("No image results found for query: {}", query);
            }
            return links;
        } catch (Exception e) {
            log.error("Error during Google Image Search for query: {}", query, e);
            return List.of();
        }
    }

    /**
     * [신규] 지정된 개수만큼 이미지를 검색합니다. (10개 이상 검색 가능)
     * @param query 검색어
     * @param totalCount 가져올 이미지 최대 개수
     * @return 이미지 URL 리스트
     */
    public List<String> searchImages(String query, int totalCount) {
        List<String> allLinks = new ArrayList<>();
        int start = 1; // Google API는 1-based index 사용

        while (allLinks.size() < totalCount) {
            int num = Math.min(10, totalCount - allLinks.size()); // 한 번에 최대 10개
            try {
                URI uri = UriComponentsBuilder.fromHttpUrl(GOOGLE_SEARCH_URL)
                        .queryParam("key", apiKey)
                        .queryParam("cx", cx)
                        .queryParam("q", query)
                        .queryParam("searchType", "image")
                        .queryParam("num", num)
                        .queryParam("start", start)
                        .queryParam("fileType", "jpg")
                        .build()
                        .toUri();

                ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
                if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                    break;
                }

                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode items = root.path("items");
                if (items.isMissingNode() || items.size() == 0) {
                    break;
                }

                for (JsonNode item : items) {
                    allLinks.add(item.path("link").asText());
                }
                start += items.size();
            } catch (Exception e) {
                log.error("Error searching images with count for query: {}", query, e);
                break;
            }
        }
        return allLinks;
    }
}
