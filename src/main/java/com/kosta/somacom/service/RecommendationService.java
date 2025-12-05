package com.kosta.somacom.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.retail.v2.BranchName;
import com.google.cloud.retail.v2.ImportMetadata;
import com.google.cloud.retail.v2.ImportProductsRequest;
import com.google.cloud.retail.v2.ImportProductsResponse;
import com.google.cloud.retail.v2.PredictRequest;
import com.google.cloud.retail.v2.PredictResponse;
import com.google.cloud.retail.v2.PredictionServiceClient;
import com.google.cloud.retail.v2.ProductDetail;
import com.google.cloud.retail.v2.UserEventServiceClient;
import com.google.cloud.retail.v2.WriteUserEventRequest;
import com.google.cloud.retail.v2.ProductInlineSource;
import com.google.cloud.retail.v2.ProductInputConfig;
import com.google.cloud.retail.v2.ProductServiceClient;
import com.google.cloud.retail.v2.UserEvent;
import com.google.protobuf.Int32Value;
import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.domain.part.RamSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.score.UserActionWeight;
import com.kosta.somacom.domain.score.UserIntentScore;
import com.kosta.somacom.dto.response.ProductSimpleResponse;
import com.kosta.somacom.dto.response.RecommendationResponseDto;
import com.kosta.somacom.engine.RuleEngineService;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.CartRepository;
import com.kosta.somacom.repository.PopularityScoreRepository;
import com.kosta.somacom.repository.ProductRepository;

import com.kosta.somacom.repository.UserIntentScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final PredictionServiceClient predictionServiceClient;
    private final ProductServiceClient productServiceClient; // 카탈로그 동기화를 위해 추가
    private final UserEventServiceClient userEventServiceClient; // Google UserEvent 전송을 위해 추가
    private final UserIntentScoreRepository userIntentScoreRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final PopularityScoreRepository popularityScoreRepository;
    private final RuleEngineService ruleEngineService;
    private final BaseSpecRepository baseSpecRepository; // DB의 모든 BaseSpec을 읽기 위해 추가

    @Value("${gcp.project-id}")
    private String projectId;
    @Value("${gcp.location}")
    private String location;

    private static final String DEFAULT_CATALOG = "default_catalog";
    private static final String DEFAULT_BRANCH = "default_branch";
    private static final String DEFAULT_PLACEMENT = "default-placement";

    // Google AI로부터 추천 후보를 넉넉하게 받아오기 위한 풀 사이즈
    private static final int RECOMMENDATION_CANDIDATE_POOL_SIZE = 20;

    /**
     * DB의 모든 BaseSpec 데이터를 Google Cloud Retail 카탈로그에 동기화(업로드)합니다.
     * @return 동기화 작업 결과 요약 문자열
     */
    public String syncCatalogFromDb() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        List<BaseSpec> allBaseSpecs = baseSpecRepository.findAll();
        if (allBaseSpecs.isEmpty()) {
            return "No BaseSpecs found in the database to sync.";
        }

        List<com.google.cloud.retail.v2.Product> productsToImport = allBaseSpecs.stream()
                .map(this::convertBaseSpecToCatalogProduct)
                .collect(Collectors.toList());

        // Google API는 한번에 100개씩만 업로드 가능하므로, 배치로 나누어 처리합니다.
        final int BATCH_SIZE = 100;
        int totalSuccessCount = 0;
        int totalFailureCount = 0;
        List<String> operationNames = new ArrayList<>();

        for (int i = 0; i < productsToImport.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, productsToImport.size());
            List<com.google.cloud.retail.v2.Product> batchProducts = productsToImport.subList(i, end);

            log.info("Importing batch {}: {} products (from index {} to {})",
                (i / BATCH_SIZE) + 1, batchProducts.size(), i, end - 1);

            ProductInlineSource inlineSource = ProductInlineSource.newBuilder()
                    .addAllProducts(batchProducts)
                    .build();

            String defaultBranch = BranchName.of(projectId, location, DEFAULT_CATALOG, DEFAULT_BRANCH).toString();
            ImportProductsRequest request = ImportProductsRequest.newBuilder()
                    .setParent(defaultBranch)
                    .setInputConfig(ProductInputConfig.newBuilder().setProductInlineSource(inlineSource).build())
                    .build();

            OperationFuture<ImportProductsResponse, ImportMetadata> operation =
                productServiceClient.importProductsAsync(request);

            operationNames.add(operation.getName());
            log.info("Catalog import started. Operation name: {}", operation.getName());

            // 5분 타임아웃 설정
            ImportMetadata metadata = operation.getMetadata().get(5, TimeUnit.MINUTES);

            totalSuccessCount += metadata.getSuccessCount();
            totalFailureCount += metadata.getFailureCount();

            if (metadata.getFailureCount() > 0) {
                log.error("Batch {} FAILED with {} errors.", (i / BATCH_SIZE) + 1, metadata.getFailureCount());
            }
        }

        String finalResult = String.format(
            "Catalog sync finished. Total Success: %d, Total Failure: %d. Operations: %s",
            totalSuccessCount, totalFailureCount, String.join(", ", operationNames)
        );
        log.info(finalResult);
        return finalResult;
    }

    /**
     * 사용자의 행동 로그를 분석하여 개인화된 추천 상품 목록을 반환합니다.
     * @param userId 사용자 ID
     * @param eventType 추천을 요청하는 페이지의 컨텍스트 (e.g., "home-page-view")
     * @param count 받고자 하는 추천 상품 개수
     * @return 추천 상품 목록 (현재는 상위 의도 태그 목록을 반환)
     */
    public List<RecommendationResponseDto> getRecommendations(String userId, String eventType, int count) throws IOException {
        log.info("==================== [Recommendation DEBUG START] ====================");
        log.info("[0] userId: {}, eventType: {}, count: {}", userId, eventType, count);
        // 1. 사용자의 모든 의도 점수 조회
        List<UserIntentScore> userScores = userIntentScoreRepository.findById_UserId(userId);
        if (userScores.isEmpty()) {
            log.info("No intent scores found for user: {}. Returning default recommendations.", userId);
            // TODO: 기본 추천 로직 (e.g., 전체 인기 상품)
            return List.of();
        }

        // 2. 가중치가 적용된 최종 의도 점수 계산
        Map<String, Double> weightedScores = calculateWeightedScores(userScores);
        log.info("[1] Calculated Weighted Scores (top 5): {}", getTopIntentsWithScores(weightedScores, 5));

        // 3. [SYS-1 연동] 장바구니 기반 호환성 태그 추출
        List<BaseSpec> itemsInCart = cartRepository.findBaseSpecsInCartByUserId(Long.valueOf(userId));
        Set<String> cartCompatibilityTags = getCompatibilityTagsFromCart(itemsInCart);
        log.info("[2] Extracted Compatibility Tags from Cart: {}", cartCompatibilityTags);

        // 4. [신규] 추천할 대상 카테고리 및 대표 상품(Seed Item) 결정
        Optional<BaseSpec> seedItemOpt = findSeedItem(weightedScores, itemsInCart);
        if (seedItemOpt.isEmpty()) {
            log.warn("Could not determine a seed item for recommendation. Aborting.");
            return List.of();
        }
        BaseSpec seedItem = seedItemOpt.get();
        log.info("[3] Seed item for 'Similar Items' model determined: {} ({})", seedItem.getName(), seedItem.getId());

        // 5. 대표 상품을 컨텍스트로 사용하여 Google AI 호출 (필터 없음)
        List<String> recommendedBaseSpecIds = callPredictionAPI(userId, eventType, "", List.of(seedItem));
        log.info("[4] Recommended BaseSpec IDs from Google AI: {}", recommendedBaseSpecIds);

        // 6. 자기 자신은 추천에서 제외
        recommendedBaseSpecIds.remove(seedItem.getId());
        log.info("[4-1] Removed self from recommendations. Current candidates: {}", recommendedBaseSpecIds);

        // 7. 백엔드에서 직접 의도 필터링 수행 (선택적: AI가 이미 유사한 아이템을 반환했으므로 생략 가능)
        // Set<String> userTopIntents = getTopIntents(weightedScores, 5).stream().collect(Collectors.toSet());
        // List<String> filteredBaseSpecIds = recommendedBaseSpecIds.stream()
        //         .filter(baseSpecId -> {
        //             BaseSpec spec = baseSpecRepository.findById(baseSpecId).orElse(null);
        //             if (spec == null) return false;
        //             Set<String> specTags = getCompatibilityTagsFromCart(List.of(spec));
        //             // 사용자의 상위 의도 태그와 상품의 태그 중 하나라도 일치하면 통과
        //             return !java.util.Collections.disjoint(userTopIntents, specTags);
        //         })
        //         .collect(Collectors.toList());
        // log.info("[4-1] After Backend Intent Filtering (Top 5 Intents: {}): {}", userTopIntents, filteredBaseSpecIds);

        // 8. [SYS-2 연동] 추천된 상품들을 인기도 점수에 따라 재정렬
        List<String> sortedBaseSpecIds = sortByPopularity(recommendedBaseSpecIds, itemsInCart);
        log.info("[5] Sorted BaseSpec IDs by Popularity: {}", sortedBaseSpecIds);

        // 9. 최종 상품 객체로 변환
        List<Product> finalProducts = convertBaseSpecsToProducts(sortedBaseSpecIds);
        log.info("[6] Converted to Product objects (count: {}): {}", finalProducts.size(), finalProducts.stream().map(p -> p.getBaseSpec().getId()).collect(Collectors.toList()));

        // 10. [신규] 호환성 검사 및 우선순위 정렬
        List<RecommendationResponseDto> finalRecommendations = checkCompatibilityAndSort(finalProducts, itemsInCart);
        log.info("[7] After Compatibility Check & Sort (count: {}): {}", finalRecommendations.size(), finalRecommendations.stream().map(r -> r.getProduct().getProductId() + ":" + r.getCompatibilityStatus()).collect(Collectors.toList()));

        // 11. 최종 개수만큼 잘라서 반환
        List<RecommendationResponseDto> limitedRecommendations = finalRecommendations.stream().limit(count).collect(Collectors.toList());
        log.info("[8] Final-Limited Recommendations (count: {}): {}", limitedRecommendations.size(), limitedRecommendations.stream().map(r -> r.getProduct().getProductId()).collect(Collectors.toList()));
        log.info("==================== [Recommendation DEBUG END] ======================");
        return limitedRecommendations;
    }

    /**
     * BaseSpec 엔티티를 Google Cloud Catalog의 Product 객체로 변환합니다.
     */
    private com.google.cloud.retail.v2.Product convertBaseSpecToCatalogProduct(BaseSpec baseSpec) {
        com.google.cloud.retail.v2.Product.Builder productBuilder = com.google.cloud.retail.v2.Product.newBuilder();

        productBuilder.setId(baseSpec.getId());
        productBuilder.setTitle(baseSpec.getName());
        productBuilder.addCategories(baseSpec.getCategory().name());

        // 호환성 태그들을 추출하여 추가합니다.
        Set<String> tags = getCompatibilityTagsFromCart(List.of(baseSpec));
        productBuilder.addAllTags(tags);

        return productBuilder.build();
    }

    /**
     * [SYS-1] 장바구니의 아이템들로부터 관련된 모든 호환성 태그를 추출합니다.
     */
    private Set<String> getCompatibilityTagsFromCart(List<BaseSpec> itemsInCart) {
        Set<String> tags = new HashSet<>();
        if (itemsInCart.isEmpty()) {
            return tags;
        }

        for (BaseSpec item : itemsInCart) {
            CpuSpec cpu = item.getCpuSpec();
            if (cpu != null) {
                tags.add("socket_" + cpu.getSocket().toLowerCase());
                if (cpu.getSupportedMemoryTypes() != null) {
                    for (String memType : cpu.getSupportedMemoryTypes().split(",")) {
                        tags.add("memory_" + memType.trim().toLowerCase());
                    }
                }
            }
            MotherboardSpec mb = item.getMotherboardSpec();
            if (mb != null) {
                tags.add("socket_" + mb.getSocket().toLowerCase());
                tags.add("chipset_" + mb.getChipset().toLowerCase());
                tags.add("memory_" + mb.getMemoryType().toLowerCase());
                if (mb.getPcieVersion() != null) {
                    tags.add("pcie_" + mb.getPcieVersion());
                }
                if (mb.getPcieLanes() != null) {
                    tags.add("pcie_lanes_" + mb.getPcieLanes());
                }
            }
            RamSpec ram = item.getRamSpec();
            if (ram != null) {
                tags.add("memory_" + ram.getMemoryType().toLowerCase());
                tags.add("speed_" + String.valueOf(ram.getSpeedMhz()));
            }
            GpuSpec gpu = item.getGpuSpec();
            if (gpu != null) {
                if (gpu.getPcieVersion() != null) {
                    tags.add("pcie_" + gpu.getPcieVersion());
                }
                if (gpu.getPcieLanes() != null) {
                    tags.add("pcie_lanes_" + gpu.getPcieLanes());
                }
            }
        }
        log.info("Extracted compatibility tags from cart: {}", tags);
        return tags;
    }

    /**
     * [신규] 사용자의 의도와 장바구니 상태를 분석하여 추천의 기준이 될 '대표 상품(Seed Item)'을 찾습니다.
     */
    private Optional<BaseSpec> findSeedItem(Map<String, Double> weightedScores, List<BaseSpec> itemsInCart) {
        // 1. 장바구니에 있는 상품들의 부품 카테고리(CPU, RAM 등)를 수집
        Set<String> cartCategories = itemsInCart.stream()
                .map(item -> item.getCategory().name())
                .collect(Collectors.toSet());

        // 2. 사용자의 의도 점수가 높은 순으로 정렬된 태그 목록
        List<String> topIntents = getTopIntents(weightedScores, 10);

        // 3. 1차 시도: 장바구니에 없는 카테고리의 "다음 부품"을 추천하기 위한 대표 상품 찾기
        for (String intentTag : topIntents) {
            String[] parts = intentTag.split("_");
            if (parts.length < 2) continue;

            String intentCategory = parts[0]; // e.g., "memory", "socket"

            // 의도 카테고리(memory)를 실제 부품 카테고리(RAM)로 변환
            List<String> targetPartCategories = mapIntentToPartCategory(intentCategory);
            if (targetPartCategories.isEmpty()) {
                continue;
            }

            // 매핑된 부품 카테고리 목록을 순회 (e.g., "socket" -> ["CPU", "MOTHERBOARD"])
            for (String targetPartCategory : targetPartCategories) {
                // 해당 부품 카테고리가 이미 장바구니에 있다면 건너뜀 (다음 부품을 추천해야 하므로)
                if (cartCategories.contains(targetPartCategory)) {
                    continue;
                }

                // 장바구니에 없는 카테고리의 의도를 발견하면, 해당 의도 태그와 일치하는 상품을 대표 상품으로 탐색
                Optional<BaseSpec> seed = findMatchingSeedItem(targetPartCategory, intentTag);
                if (seed.isPresent()) {
                    log.info("Primary seed item found (Next part recommendation for {}).", targetPartCategory);
                    return seed;
                }
            }
        }

        // 4. 2차 시도(Fallback): 1차에서 대표 상품을 못찾았다면, 최상위 의도를 기준으로 "유사 부품" 추천
        if (!topIntents.isEmpty()) {
            log.warn("Could not find a 'next part' seed item. Falling back to highest intent.");
            String highestIntent = topIntents.get(0);
            String[] parts = highestIntent.split("_");
            if (parts.length >= 2) {
                String intentCategory = parts[0];

                List<String> targetPartCategories = mapIntentToPartCategory(intentCategory);
                if (!targetPartCategories.isEmpty()) {
                    // Fallback에서는 첫 번째 매핑된 카테고리만 사용
                    String targetPartCategory = targetPartCategories.get(0);
                    Optional<BaseSpec> seed = findMatchingSeedItem(targetPartCategory, highestIntent);
                    if (seed.isPresent()) {
                        log.info("Fallback seed item found (Similar part recommendation for {}).", targetPartCategory);
                        return seed;
                    }
                }
            }
        }

        return Optional.empty(); // 적절한 대표 상품을 찾지 못함
    }

    /**
     * [신규] 의도 카테고리를 실제 부품 카테고리(Enum) 이름으로 매핑합니다.
     */
    private List<String> mapIntentToPartCategory(String intentCategory) {
        switch (intentCategory.toLowerCase()) {
            case "memory":
            case "speed":
                return List.of("RAM");
            case "socket":
                // 소켓은 CPU와 메인보드 모두와 관련이 있음
                return List.of("CPU", "Motherboard");
            case "chipset":
                return List.of("Motherboard");
            case "pcie":
            case "pcie_lanes":
                return List.of("GPU", "Motherboard"); // PCIe는 GPU와 메인보드 모두와 관련이 있음
            default:
                return List.of();
        }
    }

    /**
     * [신규] 카테고리와 속성 태그에 맞는 BaseSpec을 DB에서 찾습니다.
     */
    private Optional<BaseSpec> findMatchingSeedItem(String partCategory, String intentTag) {
        PartCategory categoryEnum = com.kosta.somacom.domain.part.PartCategory.valueOf(partCategory);
        List<BaseSpec> candidates = baseSpecRepository.findAllByCategory(categoryEnum);

        // 1. 먼저, 의도 태그와 정확히 일치하는 상품을 찾습니다.
        for (BaseSpec candidate : candidates) {
            Set<String> tags = getCompatibilityTagsFromCart(List.of(candidate));
            if (tags.contains(intentTag)) {
                return Optional.of(candidate);
            }
        }

        // 2. 정확히 일치하는 상품이 없으면, 카테고리 내에서 가장 최신(ID가 높은) 상품을 대표로 선정합니다.
        //    이는 너무 오래되거나 비주류인 상품이 대표로 선정되는 것을 방지합니다.
        log.warn("No exact match for seed item with tag '{}'. Finding the latest item in category '{}' as a fallback seed.", intentTag, partCategory);
        Optional<BaseSpec> latestInCategory = baseSpecRepository.findFirstByCategoryAndNameContainingIgnoreCaseOrderByIdDesc(categoryEnum, ""); // 모든 상품 대상
        if(latestInCategory.isPresent()){
            return latestInCategory;
        }

        return Optional.empty();
    }

    private Optional<String> findHighestIntentFilter(Map<String, Double> weightedScores, Set<String> cartCompatibilityTags) {
        if (cartCompatibilityTags.isEmpty()) {
            return Optional.empty();
        }
        return cartCompatibilityTags.stream()
                .filter(weightedScores::containsKey)
                .max(Comparator.comparing(weightedScores::get));
    }

    /**
     * 가중치가 적용된 점수 맵에서 상위 N개의 의도 태그를 추출합니다.
     */
    private List<String> getTopIntents(Map<String, Double> weightedScores, int count) {
        return weightedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(count)
                .map(Map.Entry::getKey) // "category:attributeTag" 형식의 태그만 추출
                .collect(Collectors.toList());
    }

    /**
     * 디버깅용: 가중치가 적용된 점수 맵에서 상위 N개의 의도 태그와 점수를 추출합니다.
     */
    private List<String> getTopIntentsWithScores(Map<String, Double> weightedScores, int count) {
        return weightedScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(count)
                .map(entry -> String.format("%s (Score: %.2f)", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Google Cloud Retail API를 호출하여 추천 결과를 가져옵니다.
     */
    private List<String> callPredictionAPI(String userId, String eventType, String filter, List<BaseSpec> itemsInCart) throws IOException {
        // API 요청 생성
        String placement = String.format("projects/%s/locations/%s/catalogs/%s/servingConfigs/%s",
                projectId, location, DEFAULT_CATALOG, "default-placement");
        // [수정] "유사 상품" 모델 대신 "함께 구매한 상품(FBT)" 모델이 배포된 서빙 구성을 사용합니다.
        // Google Cloud 콘솔에서 생성한 FBT용 서빙 구성 ID로 변경해야 합니다. (예: "fbt-placement")
        // String placementId = "frequently-bought-together"; // 실제 서빙 구성 ID로 교체 필요
        // String placement = String.format("projects/%s/locations/%s/catalogs/%s/servingConfigs/%s",
        //         projectId, location, DEFAULT_CATALOG, placementId);

        UserEvent.Builder userEventBuilder = UserEvent.newBuilder()
                .setVisitorId(userId)
                .setEventType(eventType); // 프론트에서 전달받은 컨텍스트 사용

        // 장바구니에 아이템이 있으면 productDetails에 추가하여 컨텍스트를 제공합니다.
        if (itemsInCart != null && !itemsInCart.isEmpty()) {
            for (BaseSpec item : itemsInCart) {
                ProductDetail productDetail = ProductDetail.newBuilder()
                        .setProduct(com.google.cloud.retail.v2.Product.newBuilder().setId(item.getId()).build())
                        .setQuantity(Int32Value.of(1)) // 기본 수량 1로 설정
                        .build();
                userEventBuilder.addProductDetails(productDetail);
            }
        }

        UserEvent userEvent = userEventBuilder.build();

        PredictRequest predictRequest = PredictRequest.newBuilder()
                .setPlacement(placement)
                .setUserEvent(userEvent)
                .setFilter(filter)
                .setPageSize(RECOMMENDATION_CANDIDATE_POOL_SIZE) // 넉넉하게 후보군 요청
                .putParams("priceRerankLevel", com.google.protobuf.Value.newBuilder().setStringValue("no-price-reranking").build())
                // [신규] 추천된 상품의 전체 데이터를 함께 반환하도록 요청 (디버깅에 유용)
                .putParams("returnProduct", com.google.protobuf.Value.newBuilder().setBoolValue(true).build())
                .build();

        // API 호출 및 결과 처리
        PredictResponse response = predictionServiceClient.predict(predictRequest);
        List<String> recommendedProductIds = new ArrayList<>();
        for (PredictResponse.PredictionResult result : response.getResultsList()) {
            recommendedProductIds.add(result.getId()); // "base_13600k" 와 같은 ID
        }
        return recommendedProductIds;
    }

    /**
     * UserIntentScore 목록을 받아 각 태그의 가중 점수를 계산합니다.
     */
    private Map<String, Double> calculateWeightedScores(List<UserIntentScore> scores) {
        Map<String, Double> tagScores = new HashMap<>();

        for (UserIntentScore score : scores) {
            String tag = score.getId().getCategory() + "_" + score.getId().getAttributeTag();
            double totalScore = 0;

            totalScore += score.getViewCount() * UserActionWeight.VIEW.getWeight();
            totalScore += score.getLongViewCount() * UserActionWeight.LONG_VIEW.getWeight();
            totalScore += score.getImageViewCount() * UserActionWeight.IMAGE_VIEW.getWeight();
            totalScore += score.getSearchCount() * UserActionWeight.SEARCH.getWeight();
            totalScore += score.getFilterCount() * UserActionWeight.FILTER.getWeight();
            totalScore += score.getWishlistCount() * UserActionWeight.WISHLIST.getWeight();
            totalScore += score.getCartCount() * UserActionWeight.CART.getWeight();
            totalScore += score.getPurchaseCount() * UserActionWeight.PURCHASE.getWeight();

            tagScores.put(tag, totalScore);
        }
        return tagScores;
    }

    /**
     * [SYS-2] 추천된 ID 목록을 인기도 점수에 따라 재정렬합니다.
     */
    private List<String> sortByPopularity(List<String> recommendedIds, List<BaseSpec> itemsInCart) {
        if (itemsInCart.isEmpty() || recommendedIds.isEmpty()) {
            return recommendedIds; // 정렬할 기준이 없으면 그대로 반환
        }

        // 장바구니의 모든 아이템과 추천된 아이템들 간의 인기도 점수를 합산
        Map<String, Long> popularityScores = popularityScoreRepository.findScoresForItems(recommendedIds,
                itemsInCart.stream().map(BaseSpec::getId).collect(Collectors.toList()));

        // 점수가 높은 순으로 정렬, 점수가 없으면 뒤로
        recommendedIds.sort(Comparator.comparing(id -> popularityScores.getOrDefault(id, 0L)).reversed());
        log.info("Sorted recommendations by popularity: {}", recommendedIds);
        return recommendedIds;
    }

    /**
     * 정렬된 BaseSpec ID 목록을 기반으로, 재고가 있고 가장 저렴한 Product 목록으로 변환합니다.
     */
    private List<Product> convertBaseSpecsToProducts(List<String> sortedBaseSpecIds) {
        if (sortedBaseSpecIds.isEmpty()) {
            return List.of();
        }

        // 추천된 모든 BaseSpec ID에 해당하는, 재고가 있고 판매 중인 모든 Product를 조회합니다.
        // 이 방식은 AI가 추천한 BaseSpec에 해당하는 다양한 판매자들의 상품을 모두 후보로 만듭니다.
        List<Product> products = productRepository.findProductsByBaseSpecIds(sortedBaseSpecIds);

        // 인기도 순서(sortedBaseSpecIds)를 유지하면서 Product 목록을 재정렬할 필요는 없습니다.
        // 호환성 및 최종 정렬 단계에서 순서가 다시 결정되기 때문입니다.
        return products;
    }

    /**
     * 추천된 상품 목록과 장바구니 아이템 간의 호환성을 검사하고, 그 결과에 따라 정렬합니다.
     */
    private List<RecommendationResponseDto> checkCompatibilityAndSort(List<Product> recommendedProducts, List<BaseSpec> itemsInCart) {
        if (itemsInCart.isEmpty()) {
            // 장바구니가 비어있으면 호환성 검사 없이 SUCCESS로 반환
            return recommendedProducts.stream()
                    .map(p -> new RecommendationResponseDto(new ProductSimpleResponse(p), CompatibilityResult.success()))
                    .collect(Collectors.toList());
        }

        List<RecommendationResponseDto> results = new ArrayList<>();
        for (Product product : recommendedProducts) {
            BaseSpec recommendedSpec = product.getBaseSpec();
            CompatibilityResult finalResult = CompatibilityResult.success();

            for (BaseSpec itemInCart : itemsInCart) {
                CompatibilityResult result = ruleEngineService.checkCompatibility(recommendedSpec, itemInCart);
                if (result.getStatus().ordinal() > finalResult.getStatus().ordinal()) {
                    finalResult = result; // 더 심각한 상태로 갱신 (FAIL > WARN > SUCCESS)
                }
            }
            results.add(new RecommendationResponseDto(new ProductSimpleResponse(product), finalResult));
        }

        // 호환성 상태에 따라 정렬 (SUCCESS -> WARN -> FAIL 순)
        results.sort(Comparator.comparing(dto -> dto.getCompatibilityStatus().ordinal()));

        return results;
    }

    /**
     * [신규] Google Cloud에 사용자 행동 로그(User Events)를 전송합니다.
     * @param userId 사용자 ID
     * @param eventType 이벤트 유형 (e.g., "detail-page-view")
     * @param productIds 이벤트와 관련된 상품(BaseSpec) ID 목록
     */
    public void ingestUserEventsToGoogleCloud(String userId, String eventType, List<String> productIds) {
        if (userId == null || eventType == null || productIds == null || productIds.isEmpty()) {
            return;
        }

        try {
            String parentCatalog = String.format("projects/%s/locations/%s/catalogs/%s", projectId, location, DEFAULT_CATALOG);

            for (String productId : productIds) {
                UserEvent.Builder userEventBuilder = UserEvent.newBuilder()
                        .setEventType(eventType)
                        .setVisitorId(userId);

                ProductDetail productDetail = ProductDetail.newBuilder()
                        .setProduct(
                                com.google.cloud.retail.v2.Product.newBuilder().setId(productId).build()
                        )
                        .build();

                userEventBuilder.addProductDetails(productDetail);

                WriteUserEventRequest writeUserEventRequest = WriteUserEventRequest.newBuilder()
                        .setParent(parentCatalog)
                        .setUserEvent(userEventBuilder.build())
                        .build();

                userEventServiceClient.writeUserEvent(writeUserEventRequest);
            }
            log.info("Successfully ingested {} events to Google Cloud for user '{}'. Product IDs: {}",
                    productIds.size(), userId, productIds);

        } catch (Exception e) {
            log.error("Failed to ingest user events to Google Cloud for user '{}'. Product IDs: {}. Error: {}",
                    userId, productIds, e.getMessage());
        }
    }
}
