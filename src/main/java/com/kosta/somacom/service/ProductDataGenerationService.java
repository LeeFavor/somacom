package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.ProductCondition;
import com.kosta.somacom.dto.request.ProductCreateRequest;
import com.kosta.somacom.repository.BaseSpecRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDataGenerationService {

    private final BaseSpecRepository baseSpecRepository;
    private final SellerProductService sellerProductService;

    /**
     * 모든 BaseSpec에 대해 지정된 판매자가 상품을 대량으로 생성합니다.
     *
     * @param sellerId        상품을 등록할 판매자의 ID
     * @param productsPerSpec 각 BaseSpec당 생성할 상품의 개수
     * @param imageUrl        모든 상품에 공통으로 적용될 이미지 파일명
     */
    @Transactional
    public void generateProductsForEachBaseSpec(Long sellerId, int productsPerSpec, String imageUrl) {
        log.info("Starting bulk product generation for sellerId: {}", sellerId);
        List<BaseSpec> allBaseSpecs = baseSpecRepository.findAll();
        if (allBaseSpecs.isEmpty()) {
            log.warn("No BaseSpecs found. Aborting product generation.");
            return;
        }

        Random random = new Random();
        int totalCreated = 0;

        for (BaseSpec baseSpec : allBaseSpecs) {
            for (int i = 1; i <= productsPerSpec; i++) {
                ProductCreateRequest request = new ProductCreateRequest();
                request.setBaseSpecId(baseSpec.getId());
                request.setName(baseSpec.getName() + " - Special Offer " + i);

                // 100.00 ~ 599.99 사이의 랜덤 가격 생성
                double randomPrice = 100 + (random.nextDouble() * 500);
                request.setPrice(BigDecimal.valueOf(Math.round(randomPrice * 100.0) / 100.0));

                // 10 ~ 100 사이의 랜덤 재고 생성
                request.setStockQuantity(10 + random.nextInt(91));

                request.setCondition(ProductCondition.New);
                request.setDescription(baseSpec.getName() + "의 상세 설명입니다. 판매자: " + sellerId);
                request.setImageUrl(imageUrl); // 모든 상품에 동일한 이미지 URL 적용

                try {
                    sellerProductService.createProduct(request, sellerId);
                    totalCreated++;
                } catch (Exception e) {
                    log.error("Failed to create product for BaseSpecId: {}", baseSpec.getId(), e);
                }
            }
        }
        log.info("Finished bulk product generation. Total products created: {}", totalCreated);
    }
}