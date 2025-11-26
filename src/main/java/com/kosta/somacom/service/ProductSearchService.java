package com.kosta.somacom.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductRepository productRepository;
    // private final UserIntentLoggingService userIntentLoggingService; // SYS-3 의존성

    public Page<ProductSimpleResponse> searchProducts(ProductSearchCondition condition, Pageable pageable, Long userId) {
        // TODO: SYS-3 사용자 의도 로깅 구현
        // if (userId != null) {
        //     // 로그인한 사용자의 경우에만 로그 기록
        // userIntentLoggingService.logSearch(userId, condition.getKeyword());
        // userIntentLoggingService.logFilter(userId, condition.getFilters());
        // }

        return productRepository.search(condition, pageable);
    }
}