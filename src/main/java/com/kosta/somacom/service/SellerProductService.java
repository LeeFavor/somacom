package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.dto.request.ProductCreateRequest;
import com.kosta.somacom.dto.response.BaseSpecSearchResponse;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerProductService {

    private final BaseSpecRepository baseSpecRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository; // 판매자 정보를 가져오기 위해 추가

    /**
     * S-201: 기반 모델 검색
     */
    public List<BaseSpecSearchResponse> searchBaseSpecs(String query) {
        // 검색 결과는 최대 10개로 제한
        PageRequest pageable = PageRequest.of(0, 10);
        List<BaseSpec> baseSpecs = baseSpecRepository.findByNameContainingIgnoreCase(query, pageable);
        return baseSpecs.stream()
                .map(BaseSpecSearchResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * S-201.3: 판매 상품 등록
     */
    @Transactional
    public Long createProduct(ProductCreateRequest request, Long sellerId) {
        // 1. 기반 모델 조회
        BaseSpec baseSpec = baseSpecRepository.findById(request.getBaseSpecId())
                .orElseThrow(() -> new EntityNotFoundException("기반 모델을 찾을 수 없습니다: " + request.getBaseSpecId()));

        // 2. 판매자 정보 조회 (실제 구현에서는 SecurityContext에서 가져와야 함)
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다: " + sellerId));

        // 3. DTO를 Product 엔티티로 변환하여 저장
        Product product = request.toEntity(baseSpec, seller);
        Product savedProduct = productRepository.save(product);
        return savedProduct.getId();
    }
}