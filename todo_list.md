<!--
=================================================================================================
SOMACOM 프로젝트 마스터 플랜 (Gemini Code Assist용 프롬프트)
=================================================================================================
Gemini, 이 파일은 SOMACOM 프로젝트의 전체 아키텍처와 개발 진행 상황을 담고 있는 **백엔드 개발 마스터 플랜**입니다.
새로운 기능을 개발하거나 기존 코드를 수정할 때, 이 파일을 가장 먼저 참고하여 맥락을 파악해주세요.

*   '➡️' 표시는 현재 또는 다음에 진행할 작업을 나타냅니다.
*   각 기능은 [상태], 기능 ID, 설명, 관련 페이지, API, 관련 테이블, 핵심 로직, **상태(Status)**, 그리고 **세부 개발 항목(Tasks)** 정보로 구성되어 있습니다.
*   이 구조를 기반으로 일관성 있는 코드를 생성하고 리뷰해주세요.
=================================================================================================
-->

# SOMACOM 프로젝트 백엔드 To-Do 리스트 (v4.0)

---

## 🧠 1. System Engines (핵심 비즈니스 로직)

이 프로젝트의 차별점인 '지능형 엔진'입니다.

✅ **[완료] `SYS-1`: 호환성 규칙 엔진 (Rule Engine)**
  - **Description**: `base_specs`의 기술 사양을 비교하여 조립 가능 여부를 판별합니다.
  - **Logic (Batch)**: `base_specs`의 모든 조합(N*N)을 검사하여 `product_compatibility_scores` 테이블에 `SUCCESS/WARN/FAIL` 상태와 `reason_code`를 미리 저장하는 배치 작업 구현.
  - **Logic (Real-time)**: 장바구니(`carts`) 조회 시, 담긴 아이템들 간의 호환성을 즉시 검증하여 사용자에게 경고 메시지를 반환하는 로직 구현.
  - **Tables**: `base_specs`, `cpu_specs`, `motherboard_specs`, `ram_specs`, `gpu_specs`, `product_compatibility_scores`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `CompatibilityRule` 인터페이스 및 구현체(e.g., `SocketRule`, `MemoryTypeRule`) 정의
    - `[x]` `RuleEngineService` 클래스 생성 (규칙들을 실행하고 결과 집계)
    - `[x]` `Spring Batch` Job 생성 (`CompatibilityBatchJob`) - 모든 `base_specs` 조합을 읽고 `RuleEngineService`를 실행하여 `product_compatibility_scores`에 저장
    - `[x]` `BatchScheduler` 생성 (매일 새벽 3시 `CompatibilityBatchJob` 실행)
    - `[x]` `CartService`에 실시간 호환성 검증 로직 추가 (`RuleEngineService` 호출)

✅ **[완료] `SYS-2`: 인기도 엔진 (Popularity Engine)**
  - **Description**: 과거 주문 데이터를 분석하여 "A를 산 사람이 B도 샀다"는 연관성을 점수화합니다.
  - **Logic**: `order_items` 테이블을 주기적으로 분석하여 (Product A, Product B) 쌍의 빈도수(Frequency)를 계산하고 `product_popularity_scores` 테이블을 갱신(Upsert)하는 배치 작업 구현.
  - **Tables**: `order_items`, `product_popularity_scores`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `Spring Batch` Job 생성 (`PopularityBatchJob`) - `order_items`를 읽어 연관 상품 쌍을 분석
    - `[x]` `PopularityScoreRepository`에 `Upsert` 로직을 위한 커스텀 메소드 추가
    - `[x]` `PopularityEngineService` 생성 (배치 작업의 핵심 로직 담당)

➡️ **[진행중] `SYS-3`: 하이브리드 추천 엔진 (Intent Engine)**
  - **Description**: 사용자의 다양한 행동(조회, 검색, 필터링 등)에서 '호환 조건 태그'를 추출하여 의도 점수를 누적합니다. 이 점수를 '행동별 가중치 테이블'과 조합하여 사용자의 숨은 의도(예: "LGA1700 소켓과 DDR5를 지원하는 부품을 찾고 있음")를 추론하고, 이를 기반으로 스마트 필터 및 추천을 제공합니다.
  - **Logic (Logging)**: 사용자가 특정 부품과 상호작용할 때, 해당 부품의 주요 사양(예: `socket_LGA1700`, `mem_DDR5`)을 태그로 추출하여 `user_intent_score` 테이블의 점수(`viewCount`, `cartCount` 등)를 증가시키는 로직 구현.
  - **Logic (Recommendation)**: `U-401` API 요청 시, `user_intent_score`와 '행동별 가중치 테이블'을 이용해 사용자의 상위 점수 태그(의도)를 계산합니다. 이 태그들을 스마트 필터로 활용하고, `SYS-1`(호환성)과 `SYS-2`(인기도) 점수를 조합하여 최종 추천 상품 목록을 반환합니다.
  - **Tables**: `user_intent_score`, `base_specs`, `products`
  - **Dependencies**: 행동별 가중치 테이블 (별도 설정 파일 또는 DB 테이블)
  - **Status**: 핵심 기능 구현 완료. AI 모델 학습 및 결과 모니터링 단계.
  - **Tasks**:
    - `[x]` Google Cloud Retail API 연동 및 "유사 상품" 모델 호출 로직 구현 완료.
    - `[x]` `UserIntentLoggingService` 및 AOP를 통한 사용자 행동 로깅 구현 완료.
    - `[x]` `RecommendationService`에 사용자 의도 분석 및 대표 상품(Seed Item) 선정 로직 구현 완료.
    - `[x]` 대량의 `BaseSpec` 및 `Product` 테스트 데이터 생성 및 DB 저장 완료.
    - `[x]` 카탈로그 동기화(`POST /api/admin/sync/catalog`) 완료.
    - `[x]` **(신규)** 사용자가 상품 상세 페이지 조회 시, 최근 조회 상품 5개를 캐싱하고 Google Cloud에 `detail-page-view` 로그를 자동으로 전송하는 로직 구현 완료. (`UserActionLoggingAspect` 수정)
    - `[➡️]` **(다음 작업)** 여러 가상 사용자로 다양한 상품 조회 시나리오를 테스트하여, AI 모델이 충분한 행동 로그를 학습하도록 데이터 축적.
    - `[ ]` 일정 시간 경과 후, 추천 API(`GET /api/recommendations/personal`)가 다양한 카테고리의 상품을 2개 이상 반환하는지 검증 및 모니터링.

---

## ✅ Admin (관리자)

- ✅ [완료] `A-101`: 판매자 가입 요청 처리
  - **Page**: `A-101`
  - **API**: `GET /api/admin/seller-requests`, `PUT /api/admin/seller-requests/{userId}/approve`
  - **Logic**: `users` 테이블에서 `role`이 `SELLER_PENDING`인 사용자를 조회하고, 승인 시 `SELLER`로 변경.
  - **Tables**: `users`, `seller_info`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `AdminController` 생성 및 엔드포인트 추가
    - `[x]` `AdminService` 생성 및 `role` 변경 메소드 추가
    - `[x]` `SellerRequestDto` 등 응답 DTO 생성

✅ **[완료] `A-102`: 회원/판매자 계정 관리**
  - **Page**: `A-102`
  - **API**: `GET /api/admin/users`, `PUT /api/admin/users/{userId}/status`
  - **Logic**: 사용자의 `status`를 `ACTIVE`, `SUSPENDED` 등으로 변경.
  - **Tables**: `users`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `AdminController`에 회원 목록 조회 및 상태 변경 엔드포인트 추가
    - `[x]` `AdminService`에 사용자 `status` 변경 메소드 추가
    - `[x]` `UserManagementResponse` DTO 생성

- ✅ **[완료] `A-201-ADD`: 신규 기반 모델 등록**
  - **Page**: `A-201-ADD`
  - **API**: `POST /api/admin/parts`
  - **Logic**: `BaseSpec`과 하위 스펙(`CpuSpec` 등)을 트랜잭션 안에서 동시에 저장.
  - **Tables**: `base_specs`, `cpu_specs`, `motherboard_specs`, `ram_specs`, `gpu_specs`
  - **Status**: 구현 및 API 테스트 완료

✅ **[완료] `A-201-LIST`: 기반 모델 목록 조회**
  - **Page**: `A-201`
  - **API**: `GET /api/admin/parts`
  - **Logic**: QueryDSL을 이용한 동적 검색 및 페이징 처리.
  - **Tables**: `base_specs`
  - **Status**: 구현 및 API 테스트 완료
  - **Tasks**:
    - `[x]` `BaseSpecRepository`에 QueryDSL 지원을 위한 Custom Repository 인터페이스/구현체 추가 (패키지 분리 및 `@QueryProjection` 제거로 빌드 문제 해결 시도)
    - `[x]` `AdminPartService`에 `BaseSpec` 목록 조회 메소드 추가 (페이징 처리 포함)
    - `[x]` `AdminPartController`에 `GET /api/admin/parts` 엔드포인트 추가
    - `[x]` `BaseSpecListResponse` DTO 생성

✅ **[완료] `A-202`: 기반 모델 수정**
  - **Page**: `A-201-ADD` (수정 모드)
  - **API**: `GET /api/admin/parts/{baseSpecId}`, `PUT /api/admin/parts/{baseSpecId}`
  - **Logic**: 기존 `BaseSpec` 및 하위 스펙 조회 및 수정.
  - **Tables**: `base_specs`, `cpu_specs`, `motherboard_specs`, `ram_specs`, `gpu_specs`
  - **Status**: 구현 및 API 테스트 완료
  - **Tasks**:
    - `[x]` `AdminPartController`에 `GET /api/admin/parts/{id}` 및 `PUT /api/admin/parts/{id}` 엔드포인트 추가
    - `[x]` `AdminPartService`에 `BaseSpec` 조회 및 수정 메소드 추가
    - `[x]` `BaseSpecUpdateRequest`, `BaseSpecDetailResponse` DTO 생성 및 관련 DTO 수정 완료

- ✅ [완료] `A-203`: 판매자의 기반 모델 등록 요청 처리
  - **Page**: `A-203`
  - **API**: `GET /api/admin/base-spec-requests`, `PUT /api/admin/base-spec-requests/{requestId}`
  - **Logic**: `base_spec_requests` 테이블의 상태를 `PENDING`에서 `APPROVED` 또는 `REJECTED`로 변경.
  - **Tables**: `base_spec_requests`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `BaseSpecRequest` Entity 생성
    - `[x]` `BaseSpecRequestRepository` 생성
    - `[x]` `AdminController`에 엔드포인트 추가
    - `[x]` `AdminService`에 요청 목록 조회 및 상태 변경 메소드 추가

✅ **[완료] `A-401`: 외부 부품 데이터 동기화**
  - **Description**: `basespec.txt` 파일에 정리된 대량의 부품 데이터를 읽어, 로컬 DB(`base_specs` 및 하위 테이블)에 등록합니다.
  - **Logic**:
    - 1. `basespec.txt` 파일을 파싱하여 `BaseSpecCreateRequest` DTO 목록으로 변환합니다.
    - 2. 각 DTO에 대해 `AdminPartService.createBaseSpec()`을 호출하여 로컬 DB에 저장합니다.
  - **Status**: 구현 완료. (`DataInitializationService`를 통해 구현됨)
  - **Tasks**:
    - `[x]` `DataInitializationService` 생성 및 `AdminPartController`에 `/initialize-from-file` API 엔드포인트 추가
    - `[x]` `basespec.txt` 파일 파싱 로직 구현
    - `[x]` `AdminPartService`를 호출하여 DB에 저장하는 로직 구현
---
---

## ✅ Seller (판매자)

- ✅ **[완료] `S-201`: 기반 모델 검색**
  - **Page**: `S-202`
  - **API**: `GET /api/seller/base-specs?query={keyword}`
  - **Logic**: 상품 등록 전, 연결할 `BaseSpec`을 이름으로 검색.
  - **Tables**: `base_specs`
  - **Status**: 구현 완료

- **[완료] `S-201.3`: 판매 상품 등록**
  - **Page**: `S-202`
  - **API**: `POST /api/seller/products`
  - **Logic**: 검색된 `base_spec_id`와 판매자 정보(`seller_id`), 가격, 재고 등을 `products` 테이블에 저장.
  - **Tables**: `products`, `base_specs`, `users`
  - **Status**: 구현 및 API 테스트 완료

- ✅ [완료] `S-201.2`: 신규 기반 모델 등록 요청
  - **Page**: `S-202`
  - **API**: `POST /api/seller/base-spec-requests`
  - **Logic**: 검색으로 찾을 수 없는 모델에 대해 카테고리, 제조사 정보를 포함하여 Admin에게 등록을 요청.
  - **Tables**: `base_spec_requests`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `SellerProductController`에 엔드포인트 추가
    - `[x]` `SellerProductService`에 요청 저장 로직 추가
    - `[x]` `BaseSpecRequestCreateDto` 생성

- ✅ [완료] `S-202`: 내 판매 상품 목록 조회
  - **Page**: `S-101` (판매자 대시보드)
  - **API**: `GET /api/seller/products`
  - **Logic**: 로그인한 판매자(`sellerId`)의 모든 상품(`products`)을 페이징하여 조회.
  - **Tables**: `products`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `SellerProductController`에 상품 목록 조회 엔드포인트 추가
    - `[x]` `SellerProductService`에 상품 목록 조회 로직 추가
    - `[x]` `ProductRepository`에 `findBySellerId` 쿼리 메소드 추가
    - `[x]` `SellerProductListResponse` DTO 생성

- ✅ [완료] `S-203`: 내 판매 상품 수정
  - **Page**: `S-203`
  - **API**: `GET /api/seller/products/{productId}`, `PUT /api/seller/products/{productId}`
  - **Logic**: 자신의 `Product` 정보(가격, 재고 등)를 수정.
  - **Tables**: `products`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `SellerProductController`에 상품 조회 및 수정 엔드포인트 추가
    - `[x]` `SellerProductService`에 상품 조회 및 수정 로직 추가 (판매자 본인 상품인지 권한 확인 필요)
    - `[x]` `ProductUpdateRequest`, `ProductUpdateFormResponse` DTO 생성

- ✅ [완료] `S-204`: 내 판매 상품 삭제
  - **Page**: `S-101` (상품 목록)
  - **API**: `DELETE /api/seller/products/{productId}`
  - **Logic**: 자신의 `Product`를 논리적으로 삭제 (Soft Delete: `is_visible`을 `false`로 변경).
  - **Tables**: `products`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `SellerProductController`에 상품 삭제 엔드포인트 추가
    - `[x]` `SellerProductService`에 Soft Delete 로직 추가 (권한 확인 포함)
    - `[x]` 모든 상품 조회 로직(`Repository`)에 `isVisible = true` 필터링 조건 추가 완료

- ✅ [완료] `S-301`: 내 상품에 대한 신규 주문 목록 조회
  - **Page**: `S-102` (판매자 주문 관리)
  - **API**: `GET /api/seller/orders`
  - **Logic**: `order_items` 테이블에서 자신의 `seller_id`와 관련된 주문 내역을 조회.
  - **Tables**: `order_items`, `orders`, `products`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `OrderItemRepository`에 판매자 ID로 조회하는 커스텀 메소드 추가 (페이징 포함)
    - `[x]` `SellerOrderController` 및 `SellerOrderService` 생성
    - `[x]` `SellerOrderResponseDto` 생성

- ✅ [완료] `S-302`: 배송 상태 변경 및 송장 번호 입력
  - **Page**: `S-102`
  - **API**: `PUT /api/seller/orders/{orderItemId}`
  - **Logic**: `order_items`의 `status`를 `PREPARING` -> `SHIPPED`로 변경하고 `tracking_number`를 업데이트.
  - **Tables**: `order_items`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `SellerOrderController`에 `PUT` 엔드포인트 추가
    - `[x]` `SellerOrderService`에 `order_item` 상태 변경 로직 추가 (권한 확인 포함)
    - `[x]` `SellerOrderItemUpdateRequest` DTO 생성

---

## 👤 User (일반 사용자)

- ✅ [완료] `U-101`: 일반 회원가입
  - **Page**: `P-103-USER`
  - **API**: `POST /api/auth/signup/user`
  - **Logic**: 이메일, 암호화된 비밀번호, 닉네임을 `users` 테이블에 저장. `role`은 `USER`로 기본 설정.
  - **Tables**: `users`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `AuthController`에 일반 회원가입 엔드포인트 추가
    - `[x]` `AuthService`에 일반 사용자 정보 저장 로직 추가
    - `[x]` `UserSignupRequest` DTO 생성

- ✅ [완료] `S-101`: 판매자 입점 신청
  - **Page**: `P-103-SELLER`
  - **API**: `POST /api/auth/signup/seller`
  - **Logic**: `users` 테이블에 `role`을 `SELLER_PENDING`으로, `seller_info` 테이블에 사업자 정보를 함께 저장.
  - **Tables**: `users`, `seller_info`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `AuthController`에 판매자 회원가입 엔드포인트 추가
    - `[x]` `AuthService`에 판매자 정보 저장 로직 추가 (트랜잭션 처리)
    - `[x]` `SellerSignupRequest` DTO 생성

- ✅ [완료] `P-201-SEARCH`: 상품 검색 (키워드, 카테고리, 상세 필터)
  - **Page**: `P-201-SEARCH`
  - **API**: `GET /api/products/search`
  - **Logic**: QueryDSL을 사용하여 `products`와 `base_specs`를 조인하고, 카테고리별 동적 필터 조건을 적용. 검색 이벤트는 `SYS-3` 엔진에 로깅.
  - **Logging**: 키워드 검색 시 `user_intent_score.searchCount` 증가, 호환성 관련 상세 필터(소켓, 칩셋 등) 적용 시 `user_intent_score.filterCount` 증가.
  - **Tables**: `products`, `base_specs`, `cpu_specs`, `...`
  - **Status**: 핵심 기능 및 로깅 연동 완료.
  - **Tasks**:
    - `[x]` `ProductRepository`에 동적 검색 기능 구현
    - `[x]` `ProductSearchController` 및 `ProductSearchService` 생성
    - `[x]` `ProductSearchRequest` DTO (동적 필터 파라미터용) 및 `ProductSearchResponse` DTO 생성
    - `[x]` `UserActionLoggingAOP`를 통해 `searchProducts` 메소드 실행 후 `SEARCH`, `FILTER` 이벤트 로깅 완료.

- ✅ [완료] `P-201.1`: 검색 자동완성
  - **Page**: `common-header`
  - **API**: `GET /api/products/autocomplete?query={keyword}`
  - **Logic**: `base_specs` 테이블에서 `name`을 기준으로 `LIKE` 검색하여 상위 N개의 모델명을 반환.
  - **Tables**: `base_specs`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `ProductSearchController`에 자동완성 엔드포인트 추가
    - `[x]` `ProductSearchService`에 자동완성 로직 추가
    - `[x]` `BaseSpecRepository`에 `findTop10ByNameContainingIgnoreCase`와 같은 쿼리 메소드 추가
    - `[x]` `AutocompleteResponse` DTO 생성

- ✅ [완료] `P-202`: 상품 상세 조회
  - **Page**: `P-202`
  - **API**: `GET /api/products/{productId}`
  - **Logic**: 상품 정보, 기반 사양, 판매자 정보와 함께 '가격 비교 목록(동일 `base_spec_id`의 다른 상품)' 및 'AI 추천 상품(`SYS-3` 호출)'을 함께 반환.
  - **Logging**: 상품 조회 시 `viewCount` 증가. 15초 이상 체류 시 `longViewCount` 증가. 상품 이미지 클릭 시 `imageViewCount` 증가.
  - **Tables**: `products`, `base_specs`, `seller_info`
  - **Status**: 핵심 기능 및 `VIEW` 이벤트 로깅 구현 완료. 프론트엔드 연동 대기.
  - **Tasks**:
    - `[x]` `ProductDetailController` 및 `ProductDetailService` 생성
    - `[x]` `ProductDetailService` 내에서 가격 비교 목록을 조합하는 로직 구현
    - `[x]` `ProductDetailResponse` DTO 생성 (여러 정보를 담는 복합 DTO)
    - `[x]` `UserActionLoggingAspect`를 통해 `getProductDetail` 호출 시 `VIEW` 이벤트 로깅 및 AI 학습 데이터 전송 완료.
    - `[➡️]` **(다음 작업)** 프론트엔드에서 특정 조건(15초 이상 체류, 이미지 클릭 등) 만족 시, `POST /api/logs/action` API를 호출하여 `LONG_VIEW`, `IMAGE_VIEW` 이벤트를 전송하도록 연동.
    - `[x]` **(즉시 구매)** `OrderService`에 단일 상품으로 주문을 생성하는 로직 추가 완료

✅ **[완료] `P-203`: 호환성 필터 적용 검색**
  - **Page**: `P-201-SEARCH`
  - **API**: `GET /api/products/search?compatFilter=true&...`
  - **Logic**: 사용자의 장바구니(`carts`)에 담긴 부품과 호환되는 부품만 필터링. (CPU-MB 소켓, GPU-MB PCIe, RAM-MB/CPU 메모리 타입 규칙 등)
  - **Tables**: `carts`, `cart_items`, `product_compatibility_scores`
  - **Status**: 모든 부품(CPU, MB, RAM, GPU) 간의 양방향 호환성 규칙 구현 완료. `SYS-1` 엔진 연동 대기.
  - **Tasks**:
    - `[x]` `ProductSearchCondition` DTO에 `boolean compatFilter` 필드 추가
    - `[x]` `ProductRepositoryImpl`의 `search` 메소드에 호환성 필터 조건(BooleanExpression) 추가
    - `[x]` `MotherboardSpec`에 PCIe 슬롯 정보 추가 (GPU 호환성 검사용)
    - `[x]` `ProductRepositoryImpl`의 `dynamicFilters` 메소드에 상세 필터링 로직 구현
    - `[x]` `ProductRepositoryImpl`의 `compatibilityFilter` 메소드에 모든 부품 간 호환성 규칙 구현 완료
    - `[x]` **(리팩토링 완료)** `ProductRepositoryImpl`의 `compatibilityFilter`를 `product_compatibility_scores` 테이블을 사용하도록 개선

- ✅ [완료] `P-301`: 장바구니 관리 (추가/조회/수정/삭제)
  - **Page**: `P-301`
  - **API**: `POST /api/cart/items`, `GET /api/cart`, `PUT /api/cart/items/{cartItemId}`, `DELETE /api/cart/items/{cartItemId}`
  - **Logic**: 장바구니 조회 시 `SYS-1` 엔진을 실시간 호출하여 전체 견적의 호환성 상태를 계산하고 응답에 포함.
  - **Logging**: 장바구니에 상품 추가 시 `user_intent_score.cartCount` 증가.
  - **Tables**: `carts`, `cart_items`, `products`
  - **Status**: 핵심 기능 및 로깅 연동 완료.
  - **Tasks**:
    - `[x]` `Cart`, `CartItem` Entity 및 Repository 생성
    - `[x]` `CartController` 및 `CartService` 생성
    - `[x]` `CartService`에 추가/조회/수정/삭제 로직 구현
    - `[~]` `CartResponse` DTO (호환성 결과 포함) 생성
    - `[x]` `UserActionLoggingAspect`를 통해 `addCartItem` 메소드 실행 후 `CART` 이벤트 로깅 완료.

✅ **[완료] `U-301.5`: 장바구니 선택 삭제**
  - **Page**: `P-301`
  - **API**: `DELETE /api/cart/items`
  - **Logic**: 요청 본문에 포함된 여러 `cartItemId`들을 한 번에 삭제.
  - **Tables**: `cart_items`
  - **Status**: 구현 완료
  - **Tasks**:
    - `[x]` `CartController`에 `DELETE /api/cart/items` 엔드포인트 추가
    - `[x]` `CartService`에 여러 아이템을 삭제하는 로직 추가
    - `[x]` `CartItemDeleteRequest` DTO 생성 (`List<Long> cartItemIds` 포함)

- ✅ [완료] `P-501`: 주문 생성 (결제)
  - **Page**: `P-302`
  - **API**: `POST /api/orders`
  - **Logic**: `orders` 및 `order_items` 생성, `products.stock_quantity` 재고 차감, `carts`에서 주문된 아이템 삭제. (트랜잭션 처리 필수)
  - **Logging**: 주문 완료 시 주문된 상품에 대해 `user_intent_score.purchaseCount` 증가.
  - **Tables**: `orders`, `order_items`, `carts`, `cart_items`
  - **Status**: 핵심 기능 및 로깅 연동 완료.
  - **Tasks**:
    - `[x]` `Order`, `OrderItem` Entity 및 Repository 생성
    - `[x]` `OrderController` 및 `OrderService` 생성
    - `[x]` `OrderService`에 주문 생성 트랜잭션 로직 구현 (재고 차감, 장바구니 비우기 포함)
    - `[x]` `OrderCreateRequest` DTO 생성
    - `[x]` `UserActionLoggingAspect`를 통해 `createOrder`, `createInstantOrder` 메소드 실행 후 `PURCHASE` 이벤트 로깅 완료.

- **[예정] `P-502`: 결제 시스템 연동**
  - **Page**: `P-302`
  - **API**: `POST /api/payments/prepare`, `POST /api/payments/complete` (예시)
  - **Logic**: 주문 생성(`P-501`) 전에 PG사(카카오페이, 토스 등)에 결제 정보를 등록하고, 결제가 완료되면 PG사로부터 받은 정보를 검증한 후 주문을 최종 생성.
  - **Tables**: `orders` (결제 정보 필드 추가 가능)
  - **Status**: 신규 추가
  - **Tasks**:
    - `[ ]` PG사 연동 라이브러리 의존성 추가
    - `[ ]` `PaymentService` 생성 (결제 준비, 완료, 검증 로직)
    - `[ ]` `OrderService`의 `createOrder` 로직을 결제 완료 후 호출되도록 수정
    - `[ ]` `PaymentController` 생성

- ✅ [완료] `P-401`: 주문 내역 조회
  - **Page**: `P-401` (마이페이지)
  - **API**: `GET /api/orders`, `GET /api/orders/{orderId}`
  - **Logic**: 로그인된 사용자의 `userId`로 `orders` 테이블을 조회. 페이징 처리. 상세 조회 시 `order_items`과 관련 `product` 정보까지 함께 반환.
  - **Tables**: `orders`, `order_items`, `products`
  - **Status**: 핵심 기능 및 인증 연동 완료.
  - **Tasks**:
    - `[x]` `OrderController`에 주문 목록 및 상세 조회 엔드포인트 추가
    - `[x]` `OrderService`에 주문 내역 조회 로직 구현 (페이징 처리 포함)
    - `[x]` `OrderRepository`에 사용자 ID로 주문을 조회하는 쿼리 메소드 추가 (페치 조인 활용)
    - `[x]` `OrderListResponseDto`, `OrderDetailResponseDto` 등 응답 DTO 생성
    - `[x]` `@AuthenticationPrincipal`을 사용하여 실제 로그인 사용자 정보 연동 완료
 
✅ **[완료] `U-504`: 회원 정보 수정**
  - **Page**: `P-401` (마이페이지)
  - **API**: `GET /api/user/me`, `PUT /api/user/me`
  - **Logic**: 로그인한 사용자의 닉네임, 비밀번호 등을 수정. (비밀번호 변경은 이 기능을 통해 처리)
  - **Tables**: `users`
  - **Status**: 구현 완료
  - **Tasks**:
    - `[x]` `UserController` 생성 및 정보 조회/수정 엔드포인트 추가
    - `[x]` `UserService` 생성 및 정보 조회/수정 로직 추가 (닉네임, 비밀번호 변경)
    - `[x]` `UserUpdateRequest`, `UserInfoResponse` DTO 생성
 
✅ **[완료] `U-505`: 회원 탈퇴**
  - **Page**: `P-401` (마이페이지)
  - **API**: `DELETE /api/user/me`
  - **Logic**: 로그인한 사용자의 계정 상태를 `DEACTIVATED`로 변경 (Soft Delete). 로그인 및 API 요청 시 비활성화 계정 차단.
  - **Tables**: `users`
  - **Status**: 구현 완료
  - **Tasks**:
    - `[x]` `UserController`에 회원 탈퇴 엔드포인트 추가
    - `[x]` `UserService`에 계정 비활성화 로직 추가
    - `[x]` `JwtAuthorizationFilter`에서 비활성화된 사용자 차단
    - `[x]` `PrincipalDetailsService`에서 로그인 시 비활성화된 사용자 차단

✅ **[완료] `P-601`: 파일 업로드 (상품 이미지 등)**
  - **Page**: `S-202`, `A-201-ADD` 등
  - **API**: `POST /api/files/upload`
  - **Logic**: 판매자 또는 관리자가 업로드한 이미지를 서버의 특정 디렉토리에 저장하고, 저장된 고유 파일명과 접근 URL을 반환.
  - **Tables**: (직접 관련 없음, `products`나 `base_specs`의 `image_url` 필드에 파일명이 저장됨) - 관련 엔티티 및 DTO 수정 완료
  - **Status**: 신규 추가
  - **Workflow**:
    - 1. 프론트엔드에서 이미지 파일 선택 시, 이 API(`POST /api/files/upload`)를 먼저 호출하여 서버에 파일을 저장하고 `fileName`을 응답받는다.
    - 2. 상품/모델 등록/수정 폼 제출 시, 1번에서 받은 `fileName`을 `imageUrl` 필드에 담아 다른 데이터와 함께 전송한다.
  - **Tasks**:
    - `[x]` `FileController` 및 `FileService` 생성
    - `[x]` `multipart/form-data` 처리를 위한 로직 구현
    - `[x]` 파일 저장 경로 `application.properties`에 설정
    - `[x]` `FileUploadResponse` DTO 생성 (저장된 파일명 포함)


## 🔐 공통 (보안 및 인증)

- ✅ [완료] `U-102`: 로그인 (JWT 발급)
  - **Page**: `P-102-*`
  - **API**: `POST /api/auth/login`, (로그아웃은 프론트엔드에서 토큰 제거로 처리)
  - **Logic**: 이메일/비밀번호 검증 후 역할(`role`) 정보가 포함된 Access/Refresh Token 발급.
  - **Tables**: `users`
  - **Status**: 구현 및 테스트 완료.
  - **Tasks**:
    - `[x]` `spring-boot-starter-security` 의존성 추가
    - `[x]` `jjwt` 라이브러리 의존성 추가
    - `[x]` `JwtTokenProvider` 클래스 생성 (토큰 생성, 검증, 정보 추출)
    - `[x]` `UserDetailsService` 구현체 생성
    - `[x]` `SecurityConfig` 클래스 생성 (URL별 접근 권한 설정)
    - `[x]` `JwtAuthenticationFilter`, `JwtAuthorizationFilter` 생성 및 수정

- ✅ [완료] API 접근 제어 설정
  - **Page**: N/A
  - **API**: 모든 API
  - **Logic**: Spring Security를 사용하여 각 API 엔드포인트에 역할(`USER`, `SELLER`, `ADMIN`) 기반 접근 권한 설정.
  - **Status**: `SecurityConfig` 기본 설정 및 `@AuthenticationPrincipal`을 통한 사용자 정보 연동 완료.
  - **Tasks**:
    - `[x]` `SecurityConfig`의 `configure(HttpSecurity http)` 메소드에 `antMatchers`를 사용하여 URL별 권한 설정
    - `[x]` `@AuthenticationPrincipal`을 사용하여 Controller에서 로그인 사용자 정보 획득
    - `[ ]` (선택) `@PreAuthorize` 어노테이션을 사용한 메소드 레벨 세부 권한 설정
