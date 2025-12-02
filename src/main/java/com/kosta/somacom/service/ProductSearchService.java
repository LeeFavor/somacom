package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.dto.response.AutocompleteResponse;
import com.kosta.somacom.repository.BaseSpecRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;
    private final BaseSpecRepository baseSpecRepository;
    // private final UserIntentLoggingService userIntentLoggingService; // SYS-3 의존성

    public Page<ProductSimpleResponse> searchProducts(ProductSearchCondition condition, Pageable pageable, Long userId) {
        // TODO: SYS-3 사용자 의도 로깅 구현
        // if (userId != null) {
        //     // 로그인한 사용자의 경우에만 로그 기록
        // userIntentLoggingService.logSearch(userId, condition.getKeyword());
        condition.setUserId(userId); // 호환성 필터를 위해 userId 설정
        // userIntentLoggingService.logFilter(userId, condition.getFilters());
        // }

        return productRepository.search(condition, pageable);
    }

    public List<AutocompleteResponse> getAutocompleteSuggestions(String query) {
        List<BaseSpec> results = baseSpecRepository.findTop10ByNameContainingIgnoreCase(query);
        return results.stream()
                .map(baseSpec -> new AutocompleteResponse(baseSpec.getName()))
                .collect(Collectors.toList());
    }
}