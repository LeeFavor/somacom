package com.kosta.somacom.service;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.retail.v2.*;
import com.google.cloud.retail.v2.PredictResponse; // PredictResponse 임포트
import com.google.protobuf.FieldMask;
import com.google.protobuf.Int32Value; // Int32Value 래퍼 클래스 임포트
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
// [삭제] PlacementName 임포트 제거
// import com.google.cloud.retail.v2.PlacementName; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit; // [신규] Timeout 임포트
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * AI 추천 엔진 3단계(카탈로그, 로그, 추천)를 실제로 수행하는 서비스
 */
@Service
public class RecommendationTestService {

    @Autowired
    private ProductServiceClient productServiceClient;
    @Autowired
    private UserEventServiceClient userEventServiceClient;
    @Autowired
    private PredictionServiceClient predictionServiceClient;

    @Value("${gcp.project-id}")
    private String projectId;
    @Value("${gcp.location}")
    private String location;

    // Google Cloud 카탈로그의 기본 브랜치 경로
    private String getDefaultBranch() {
        // 형식: "projects/{projectId}/locations/{location}/catalogs/default_catalog/branches/default_branch"
        return BranchName.of(projectId, location, "default_catalog", "default_branch").toString();
    }
    
    // Google Cloud 카탈로그 경로
    private String getDefaultCatalog() {
        return CatalogName.of(projectId, location, "default_catalog").toString();
    }

    /**
     * [1단계] 카탈로그 저장
     * "유사 품목" 모델의 최소 요구사항(100개)을 만족시키기 위해 101개의 제품을 전송합니다.
     * [수정 v3.19] 100개 제한으로 인해 2번의 배치로 나누어 전송합니다.
     */
    public String importCatalog() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        
        // 1. ai_catalog_sample.md의 JSON 데이터 (11개 실제 모델)
        List<String> realCatalogJsonList = Arrays.asList(
            "{ \"id\": \"base_13600k\", \"title\": \"Intel Core i5-13600K\", \"categories\": [\"CPU\"], \"tags\": [\"socket_LGA1700\", \"mem_DDR5\", \"mem_DDR4\"] }",
            "{ \"id\": \"base_7600\", \"title\": \"AMD Ryzen 5 7600\", \"categories\": [\"CPU\"], \"tags\": [\"socket_AM5\", \"mem_DDR5\"] }",
            "{ \"id\": \"base_14700k\", \"title\": \"Intel Core i7-14700K\", \"categories\": [\"CPU\"], \"tags\": [\"socket_LGA1700\", \"mem_DDR5\", \"mem_DDR4\"] }",
            "{ \"id\": \"base_z790_d5\", \"title\": \"Gigabyte Z790 Aorus Elite (DDR5)\", \"categories\": [\"Motherboard\"], \"tags\": [\"socket_LGA1700\", \"mem_DDR5\"] }",
            "{ \"id\": \"base_b760_d4\", \"title\": \"ASUS B760M (DDR4)\", \"categories\": [\"Motherboard\"], \"tags\": [\"socket_LGA1700\", \"mem_DDR4\"] }",
            "{ \"id\": \"base_b650\", \"title\": \"MSI B650M (AM5)\", \"categories\": [\"Motherboard\"], \"tags\": [\"socket_AM5\", \"mem_DDR5\"] }",
            "{ \"id\": \"base_ram_d5_16g\", \"title\": \"Samsung DDR5 16GB 5600MHz\", \"categories\": [\"RAM\"], \"tags\": [\"mem_DDR5\", \"speed_5600\"] }",
            "{ \"id\": \"base_ram_d5_32g_gskill\", \"title\": \"G.Skill Trident Z5 DDR5 32GB Kit\", \"categories\": [\"RAM\"], \"tags\": [\"mem_DDR5\", \"speed_6000\"] }",
            "{ \"id\": \"base_ram_d4_16g\", \"title\": \"SK Hynix DDR4 16GB 3200MHz\", \"categories\": [\"RAM\"], \"tags\": [\"mem_DDR4\", \"speed_3200\"] }",
            "{ \"id\": \"base_gpu_4070\", \"title\": \"NVIDIA GeForce RTX 4070\", \"categories\": [\"GPU\"], \"tags\": [\"pcie_4.0\"] }",
            "{ \"id\": \"base_gpu_4060\", \"title\": \"NVIDIA GeForce RTX 4060\", \"categories\": [\"GPU\"], \"tags\": [\"pcie_4.0\"] }"
        );

        // 90개의 더미 제품 생성 (총 101개)
        List<Product> products = new ArrayList<>();
        for (String jsonString : realCatalogJsonList) {
            Product.Builder builder = Product.newBuilder();
            JsonFormat.parser().ignoringUnknownFields().merge(jsonString, builder);
            products.add(builder.build());
        }

        for (int i = 0; i < 90; i++) {
            String dummyId = "dummy_product_" + i;
            Product dummyProduct = Product.newBuilder()
                .setId(dummyId)
                .setTitle("Dummy Product " + i)
                .addCategories("Dummy")
                .addTags("dummy_tag")
                .build();
            products.add(dummyProduct);
        }

        // [수정 v3.19] 100개씩 나눠서(Batch) Import 요청 생성 및 호출
        final int BATCH_SIZE = 100;
        int totalSuccessCount = 0;
        int totalFailureCount = 0;
        List<String> operationNames = new ArrayList<>();

        for (int i = 0; i < products.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, products.size());
            List<Product> batchProducts = products.subList(i, end);

            System.out.println(String.format("Importing batch %d: %d products (from index %d to %d)", 
                (i / BATCH_SIZE) + 1, batchProducts.size(), i, end - 1));

            ProductInlineSource inlineSource = ProductInlineSource.newBuilder()
                    .addAllProducts(batchProducts)
                    .build();

            ImportProductsRequest request = ImportProductsRequest.newBuilder()
                    .setParent(getDefaultBranch())
                    .setInputConfig(ProductInputConfig.newBuilder().setProductInlineSource(inlineSource).build())
                    .build();

            // API 호출 (비동기)
            OperationFuture<ImportProductsResponse, ImportMetadata> operation = 
                productServiceClient.importProductsAsync(request);
            
            operationNames.add(operation.getName());
            System.out.println("Catalog import started. Operation name: " + operation.getName());
            
            // 이 배치의 완료를 기다림 (unpacking 에러 방지)
            // 5분 타임아웃 설정
            ImportMetadata metadata = operation.getMetadata().get(5, TimeUnit.MINUTES);

            // 결과 집계
            totalSuccessCount += metadata.getSuccessCount();
            totalFailureCount += metadata.getFailureCount();
            
            if (metadata.getFailureCount() > 0) {
                 System.err.println("Batch " + ((i / BATCH_SIZE) + 1) + " FAILED with " + metadata.getFailureCount() + " errors.");
            }
        }

        // 7. 최종 결과 로깅
        String finalResult;
        if (totalFailureCount > 0) {
            finalResult = String.format(
                "Catalog import finished with %d total errors. Success Count: %d. (GCP 콘솔에서 상세 에러 확인 필요)",
                totalFailureCount,
                totalSuccessCount
            );
        } else {
             finalResult = String.format(
                "Catalog import SUCCESS. Total Success Count: %d. Total Failure Count: 0.",
                totalSuccessCount
            );
        }
        
        System.out.println(finalResult);
        return finalResult + " Operations: " + String.join(", ", operationNames);
    }

    /**
     * [2단계] 사용자 로그 저장 (v3.13 - 커스텀 이벤트 처리)
     * 시뮬레이션의 `user_001` 로그 1건을 AI 엔진에 전송합니다.
     */
    public String ingestUserEvent(String userId, String eventType, String eventContext) {
        
        // 1. 이벤트 타임스탬프 생성
        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder().setSeconds(now.getEpochSecond()).setNanos(now.getNano()).build();
        
        // 2. UserEvent 기본 빌더 생성
        UserEvent.Builder userEventBuilder = UserEvent.newBuilder()
                .setEventType(eventType) // 예: "add-to-cart", "detail-page-view", "search"
                .setVisitorId(userId) // 사용자 ID
                .setEventTime(timestamp);

        // 3. 이벤트 유형에 따라 분기 처리
        // 'search' 또는 'filter-apply' (U-705) 같은 상품과 무관한 이벤트
        if ("search".equals(eventType) || "filter-apply".equals(eventType)) {
            userEventBuilder.setSearchQuery(eventContext); // (eventContext 변수를 검색어 텍스트로 사용)
        } 
        // 'detail-page-view', 'add-to-cart', 'image-zoom' (U-703) 등 상품 ID가 필요한 이벤트
        else {
            String productId = eventContext;
            
            // [수정 v3.12] Product 객체 생성 (name과 id 모두 설정)
            Product product = Product.newBuilder()
                    .setId(productId) // [필수] 짧은 ID
                    .setName(ProductName.of(projectId, location, "default_catalog", "default_branch", productId).toString()) // [선택] 전체 경로
                    .build();

            // ProductDetail 객체 생성
            ProductDetail productDetail = ProductDetail.newBuilder()
                    .setProduct(product)
                    .setQuantity(Int32Value.of(1)) // Int32Value 래퍼 사용
                    .build();
            
            userEventBuilder.addProductDetails(productDetail);
        }

        // 4. API 요청 객체 생성
        WriteUserEventRequest request = WriteUserEventRequest.newBuilder()
                .setParent(getDefaultCatalog())
                .setUserEvent(userEventBuilder.build())
                .build();

        // 5. API 호출 (동기)
        UserEvent response = userEventServiceClient.writeUserEvent(request);
        
        String result = "User event ingested: " + eventType + " for " + eventContext;
        System.out.println(result);
        return result;
    }

    /**
     * [3단계-A] 추천 요청 ("자주 함께 구매하는 항목" - FBT)
     * `user_001`의 의도를 기반으로 RAM 2종류를 추천받습니다.
     * (참고: 이 모델은 대규모 'purchase-complete' 로그가 필요하여 테스트가 어려울 수 있습니다.)
     */
    public List<PredictResponse.PredictionResult> getRecommendations(
        String userId, List<String> cartItems) {
        
        // GCP 콘솔에서 생성한 "PLACEMENT_ID"로 교체해야 합니다. (예: "default-placement")
        // [수정 v3.16] PlacementName 클래스 대신 String.format() 사용
        String placement = String.format(
            "projects/%s/locations/%s/catalogs/default_catalog/servingConfigs/%s",
            projectId, location, "default-placement"
        );

        // 1. 추천 컨텍스트(장바구니) 생성
        UserEvent.Builder userEventBuilder = UserEvent.newBuilder()
                .setVisitorId(userId)
                .setEventType("shopping-cart-page-view"); // 현재 페이지 컨텍스트

        for (String cartItemId : cartItems) {
            Product product = Product.newBuilder()
                    // [수정 v3.12] FBT 모델은 ID만 필요할 수 있으나, 일관성을 위해 name/id 모두 설정
                    .setId(cartItemId)
                    .setName(ProductName.of(projectId, location, "default_catalog", "default_branch", cartItemId).toString())
                    .build();

            ProductDetail productDetail = ProductDetail.newBuilder()
                .setProduct(product)
                .setQuantity(Int32Value.of(1)) // Int32Value 래퍼 사용
                .build();
            userEventBuilder.addProductDetails(productDetail);
        }

        // 2. API 요청 생성
        PredictRequest request = PredictRequest.newBuilder()
                .setPlacement(placement)
                .setUserEvent(userEventBuilder.build())
                // [FIX 5] 'categories' 필터 대신 'tag' 필터 사용 (API 예시 기반)
                // "RAM" 카테고리 대신 "mem_DDR5" 태그로 RAM 필터링
                .setFilter("tag=\"mem_DDR5\"") 
                .setPageSize(2) // 2개 요청
                .build();

        // 3. API 호출
        PredictResponse response = predictionServiceClient.predict(request);

        System.out.println("Recommendation results (FBT):");
        response.getResultsList().forEach(System.out::println);
        
        return response.getResultsList();
    }

    /**
     * [3단계-B] 추천 요청 ("유사 품목" - Similar Items)
     * `base_14700k`와 유사한 CPU 2종류를 추천받습니다.
     * (참고: 이 모델은 100개 이상의 카탈로그만 있으면 작동하므로 테스트가 용이합니다.)
     */
    public List<PredictResponse.PredictionResult> getSimilarItems(String referenceProductId) {
        
        // GCP 콘솔에서 생성한 "PLACEMENT_ID" (예: "default-placement")
        // [수정 v3.16] PlacementName 클래스 대신 String.format() 사용
        String placement = String.format(
            "projects/%s/locations/%s/catalogs/default_catalog/servingConfigs/%s",
            projectId, location, "default-placement"
        );

        // 1. 추천 컨텍스트(현재 보고 있는 상품) 생성
        UserEvent.Builder userEventBuilder = UserEvent.newBuilder()
                .setVisitorId("user_001_similar_test") // (테스트용 임시 ID)
                .setEventType("detail-page-view"); // 현재 페이지 컨텍스트

        Product product = Product.newBuilder()
                .setId(referenceProductId)
                .setName(ProductName.of(projectId, location, "default_catalog", "default_branch", referenceProductId).toString())
                .build();

        ProductDetail productDetail = ProductDetail.newBuilder()
            .setProduct(product)
            .setQuantity(Int32Value.of(1))
            .build();
        userEventBuilder.addProductDetails(productDetail);

        // 2. API 요청 생성
        PredictRequest request = PredictRequest.newBuilder()
                .setPlacement(placement)
                .setUserEvent(userEventBuilder.build())
                // [FIX 5] 'categories' 필터 대신 'tag' 필터 사용 (API 예시 기반)
                // "CPU" 카테고리 대신 "socket_LGA1700" 태그로 CPU 필터링
                .setFilter("tag=\"socket_LGA1700\"") 
                .setPageSize(2) // 2개 요청
                .build();

        // 3. API 호출
        PredictResponse response = predictionServiceClient.predict(request);

        System.out.println("Recommendation results (Similar Items for " + referenceProductId + "):");
        response.getResultsList().forEach(System.out::println);
        
        return response.getResultsList();
    }
}