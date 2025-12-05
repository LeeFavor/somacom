package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.dto.response.AutocompleteResponse;
import com.kosta.somacom.repository.BaseSpecRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.repository.ProductRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    /**
     * [신규] 모든 카테고리 이름을 문자열 리스트로 반환합니다.
     */
    public List<String> getAllCategories() {
        return Arrays.stream(PartCategory.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    /**
     * [신규] 특정 카테고리에 대한 필터 옵션들을 조회합니다.
     * @param categoryName CPU, GPU 등 카테고리 이름
     * @return 필터 그룹과 옵션 값들의 맵 (e.g., {"socket": ["LGA1700", "AM5"]})
     */
    public Map<String, Set<String>> getFilterOptionsForCategory(String categoryName) {
        PartCategory category;
        try {
            category = PartCategory.valueOf(categoryName);
//            System.out.println("111111"+category);
        } catch (IllegalArgumentException e) {
            return Map.of(); // 유효하지 않은 카테고리면 빈 맵 반환
        }
        switch (category) {
            case CPU:
                return Map.of(
                        "socket", baseSpecRepository.findDistinctCpuSockets(),
                        "supportedMemoryTypes", baseSpecRepository.findDistinctCpuMemoryTypes()
                );
            case Motherboard:
                return Map.of(
                        "chipset", baseSpecRepository.findDistinctMotherboardChipsets(),
                        "memoryType", baseSpecRepository.findDistinctMotherboardMemoryTypes()
                );
            case RAM:
                return Map.of(
                        "memoryType", baseSpecRepository.findDistinctRamMemoryTypes(),
                        "speedMhz", baseSpecRepository.findDistinctRamSpeeds().stream()
                                .map(String::valueOf).collect(Collectors.toSet())
                );
            case GPU:
                return Map.of(
                        "pcieVersion", baseSpecRepository.findDistinctGpuPcieVersions().stream()
                                .map(String::valueOf).collect(Collectors.toSet())
                );
            // 다른 카테고리(GPU, RAM 등)에 대한 로직 추가 가능
            default:
                return Map.of();
        }
    }
}