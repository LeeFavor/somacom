package com.kosta.somacom.service;

import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.dto.request.PriceComparisonDto;
import com.kosta.somacom.dto.response.ProductDetailResponse;
import com.kosta.somacom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductDetailService {

    private final ProductRepository productRepository;

    public ProductDetailResponse getProductDetail(Long productId) {
        // 1. Product와 BaseSpec, 그리고 모든 하위 Spec(CpuSpec 등)을 페치 조인으로 한번에 조회
        // ProductRepository에 findWithAllDetailsById 쿼리 추가 필요
        Product product = productRepository.findWithAllDetailsById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

        // 2. 가격 비교 목록 조회를 위한 페치 조인 쿼리 실행
        List<Product> comparisonProducts = productRepository.findWithSellerByBaseSpecId(product.getBaseSpec().getId());

        // 3. 지연 로딩된 SellerInfo에 접근하여 초기화 (NPE 방지)
        // 이 작업은 @Transactional 내에서 수행되어야 합니다.
        product.getSeller().getSellerInfo().getCompanyName(); // 명시적으로 접근

        List<PriceComparisonDto> priceComparisonList = comparisonProducts.stream()
                .map(PriceComparisonDto::new)
                .collect(Collectors.toList());

        // TODO: SYS-3 사용자 의도 로깅 (viewCount 증가)
        // userIntentLoggingService.logView(userId, product.getBaseSpec().getId());

        // 3. 최종 응답 DTO 조립
        return new ProductDetailResponse(product, priceComparisonList); // 이제 product는 모든 데이터를 가짐
    }
}