package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductImageBatchService {

    private final BaseSpecRepository baseSpecRepository;
    private final ProductRepository productRepository;
    private final GoogleImageSearchService googleImageSearchService;
    private final ImageDownloadService imageDownloadService;

    private static final String DEFAULT_IMAGE_NAME = "6db820b7-2d1f-47fc-9172-1ddbc0b646c5.jpg";
//    private static final int MAX_API_CALLS = 100;

    /**
     * 모든 BaseSpec을 순회하며 이미지를 검색/다운로드하고, 연관된 Product의 image_url을 업데이트합니다.
     * 비동기로 실행하여 요청 스레드를 차단하지 않습니다.
     */
    @Async
    // @Transactional 제거: 네트워크 I/O(이미지 다운로드)가 포함된 긴 작업이므로 트랜잭션을 길게 유지하지 않음
    public void runBatchUpdate() {
        log.info("Starting Product Image Update Batch Job...");

        List<BaseSpec> baseSpecs = baseSpecRepository.findAll();
        int successCount = 0;
        int failCount = 0;
        int skipCount = 0;
        int apiCallCount = 0;

        for (BaseSpec baseSpec : baseSpecs) {
            List<Product> products = productRepository.findByBaseSpecId(baseSpec.getId());
            if (products.isEmpty()) {
                continue;
            }

            // 1. 현재 BaseSpec에 속한 상품들 중 유효한(디폴트가 아닌) 이미지가 있는지 확인
            String validImageName = null;
            for (Product p : products) {
                String img = p.getImage_url();
                if (StringUtils.hasText(img) && !img.equals(DEFAULT_IMAGE_NAME) && !img.contains("/")) {
                    validImageName = img;
                    break;
                }
            }

            // 2. 업데이트가 필요한 상품 식별 (이미지가 없거나 디폴트 이미지인 경우)
            List<Product> productsToUpdate = products.stream()
                    .filter(p -> !StringUtils.hasText(p.getImage_url()) || p.getImage_url().equals(DEFAULT_IMAGE_NAME) || p.getImage_url().contains("/"))
                    .collect(Collectors.toList());

            if (productsToUpdate.isEmpty()) {
                skipCount++;
                continue;
            }

            boolean imageDownloaded = false;
            String targetImageName = validImageName;

            // 3. 유효한 이미지가 없으면 구글 검색 시도
            if (targetImageName == null) {
//                if (apiCallCount >= MAX_API_CALLS) {
//                    log.info("Daily Google API limit reached ({} calls). Stopping batch job.", apiCallCount);
//                    break;
//                }

                // -site: 옵션을 사용하여 봇 접근을 차단하는 특정 사이트들을 검색 결과에서 제외
                String query = baseSpec.getName() + " official product image white background -site:thepcwholesale.com";
                // 상위 3개 이미지를 가져와서 순차적으로 다운로드 시도
                List<String> imageUrls = googleImageSearchService.searchImages(query);
                apiCallCount++;

                if (!imageUrls.isEmpty()) {
                    // 파일명 생성: product_{baseSpecId}.jpg
                    // 파일명에 포함될 수 없는 문자(/)를 제거하여 저장 오류 방지
                    String newFileName = "product_" + baseSpec.getId().replace("/", "") + ".jpg";
                    
                    for (String imageUrl : imageUrls) {
                        if (imageDownloadService.downloadAndSave(imageUrl, newFileName)) {
                            targetImageName = newFileName;
                            imageDownloaded = true;
                            break; // 성공하면 루프 종료
                        }
                    }
                }
                
                if (!imageDownloaded) {
                    failCount++;
                }
            }

            // 4. Product 테이블 업데이트
            if (targetImageName != null) {
                for (Product product : productsToUpdate) {
                    product.updateImageUrl(targetImageName);
                }
                // 트랜잭션이 없으므로 변경 사항을 명시적으로 저장해야 함
                productRepository.saveAll(productsToUpdate);

                // 검색해서 다운로드했거나, 기존 유효 이미지를 사용했으면 성공으로 간주
                if (imageDownloaded || validImageName != null) {
                    successCount++;
                }
            }

            // 5. Rate Limiting (API 차단 방지)
            if (imageDownloaded) {
                try {
                    Thread.sleep(200); // 0.2초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Batch job interrupted");
                    break;
                }
            }
        }

        log.info("Batch Job Finished. Processed BaseSpecs: {}, Updated Groups: {}, Failed Groups: {}, Skipped Groups: {}, API Calls: {}", 
                baseSpecs.size(), successCount, failCount, skipCount, apiCallCount);
    }

    /**
     * [신규] 각 BaseSpec별로 20개의 다양한 이미지를 수집하여 Product들에 분산 배정합니다.
     * 이미지가 깨지거나 다운로드 실패 시, 성공한 이미지들만으로 순환 배정합니다.
     */
    @Async
    public void runDiverseImageBatchUpdate() {
        log.info("Starting Diverse Product Image Update Batch Job...");

        List<BaseSpec> baseSpecs = baseSpecRepository.findAll();
        int processedCount = 0;

        for (BaseSpec baseSpec : baseSpecs) {
            List<Product> products = productRepository.findByBaseSpecId(baseSpec.getId());
            if (products.isEmpty()) {
                continue;
            }

            // 1. 20개 이미지 검색
            String query = baseSpec.getName() + " official product image white background -site:thepcwholesale.com";
            List<String> imageUrls = googleImageSearchService.searchImages(query, 20);

            // [수정] 중복된 이미지 URL 제거 (동일한 이미지가 다른 파일명으로 저장되는 것 방지)
            imageUrls = imageUrls.stream().distinct().collect(Collectors.toList());

            List<String> savedFileNames = new ArrayList<>();
            int downloadIndex = 0;

            // 2. 이미지 다운로드 및 저장 (파일명에 인덱스 추가하여 구분)
            for (String imageUrl : imageUrls) {
                // 파일명: product_{baseSpecId}_{index}.jpg (슬래시 제거 포함)
                String safeBaseSpecId = baseSpec.getId().replace("/", "");
                String newFileName = "product_" + safeBaseSpecId + "_" + downloadIndex + ".jpg";

                if (imageDownloadService.downloadAndSave(imageUrl, newFileName)) {
                    savedFileNames.add(newFileName);
                    downloadIndex++;
                }
            }

            // 3. Product에 이미지 배정 (Round-Robin 방식)
            if (!savedFileNames.isEmpty()) {
                int assignIndex = 0;
                for (Product product : products) {
                    // 성공한 이미지 리스트를 순환하며 배정
                    String targetImage = savedFileNames.get(assignIndex % savedFileNames.size());
                    product.updateImageUrl(targetImage);
                    assignIndex++;
                }
                productRepository.saveAll(products);
                processedCount++;
            } else {
                log.warn("No valid images downloaded for BaseSpec: {}. Keeping existing images.", baseSpec.getId());
                // 20개 모두 실패 시 업데이트하지 않음 (기존 유지)
            }

            // API Rate Limit 조절 (너무 빠른 요청 방지)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        log.info("Diverse Batch Job Finished. Processed BaseSpecs: {}", processedCount);
    }
}
