package com.kosta.somacom.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.dto.request.ProductCreateRequest;
import com.kosta.somacom.dto.request.BaseSpecRequestCreateDto;
import com.kosta.somacom.dto.request.ProductUpdateRequest;
import com.kosta.somacom.dto.response.BaseSpecSearchResponse;
import com.kosta.somacom.dto.response.ProductUpdateFormResponse;
import com.kosta.somacom.dto.response.SellerProductListResponse;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.repository.UserRepository;
import com.kosta.somacom.repository.BaseSpecRequestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SellerProductService {

    private final BaseSpecRepository baseSpecRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository; // 판매자 정보를 가져오기 위해 추가
    private final BaseSpecRequestRepository baseSpecRequestRepository;

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
    
    /**
     * S-201.2: 신규 기반 모델 등록 요청
     */
    @Transactional
    public Long requestNewBaseSpec(BaseSpecRequestCreateDto dto, Long sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("판매자를 찾을 수 없습니다: " + sellerId));

        BaseSpecRequest request = BaseSpecRequest.builder()
                .seller(seller)
                .requestedModelName(dto.getRequestedModelName())
                .category(dto.getCategory())
                .manufacturer(dto.getManufacturer())
                .status(BaseSpecRequestStatus.PENDING)
                .build();

        return baseSpecRequestRepository.save(request).getId();
    }

    @Transactional(readOnly = true)
    public ProductUpdateFormResponse getProductForUpdate(Long productId, Long sellerId) {
        Product product = findProductAndCheckOwnership(productId, sellerId);
        return new ProductUpdateFormResponse(product);
    }
    
    @Transactional(readOnly = true)
    public Page<SellerProductListResponse> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerIdAndIsVisibleTrue(sellerId, pageable)
                .map(SellerProductListResponse::new);
    }

    @Transactional
    public void updateProduct(Long productId, Long sellerId, ProductUpdateRequest request) {
        Product product = findProductAndCheckOwnership(productId, sellerId);

        product.updateDetails(
                request.getName(),
                request.getPrice(),
                request.getStockQuantity(),
                request.getCondition(),
                request.getDescription()
        );
    }

    private Product findProductAndCheckOwnership(Long productId, Long sellerId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + productId));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new SecurityException("You do not have permission to modify this product.");
        }
        return product;
    }
    
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = findProductAndCheckOwnership(productId, sellerId);
        product.softDelete();
    }
}