# SOMACOM 프론트엔드 API 명세서

이 문서는 SOMACOM 프로젝트의 프론트엔드 개발에 필요한 **사용자, 판매자, 관리자** 역할별 주요 API의 명세를 정리합니다.

---

## 🔐 1. 인증/보안 (Auth & Security)

### 1.1. `U-101`: 일반 회원 가입

-   **API**: `POST /api/auth/signup/user`
-   **설명**: 일반 사용자 계정을 생성합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "email": "user@example.com",
          "password": "password123",
          "username": "SOMA_USER"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 사용자의 `userId` (e.g., `1`)
-   **Axios 예시**
    ```javascript
    // 일반 사용자 회원가입 요청
    const signupUser = async (email, password, username) => {
      try {
        const response = await axios.post('/api/auth/signup/user', {
          email,
          password,
          username,
        });
        console.log('회원가입 성공! User ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('회원가입 실패:', error.response.data);
      }
    };
    ```

### 1.2. `S-101`: 판매자 입점 신청

-   **API**: `POST /api/auth/signup/seller`
-   **설명**: 판매자 계정 생성을 요청합니다. 가입 후 관리자의 승인이 필요합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "email": "seller@example.com",
          "password": "password123",
          "username": "SOMA_SELLER",
          "companyName": "소마전자",
          "companyNumber": "123-45-67890",
          "phoneNumber": "010-1234-5678"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 판매자의 `userId` (e.g., `2`)
-   **Axios 예시**
    ```javascript
    // 판매자 입점 신청
    const signupSeller = async (sellerData) => {
      try {
        const response = await axios.post('/api/auth/signup/seller', sellerData);
        console.log('판매자 입점 신청 성공! User ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('판매자 입점 신청 실패:', error.response.data);
      }
    };
    ```

### 1.3. `U-102`: 로그인

-   **API**: `POST /api/auth/login`
-   **설명**: 이메일과 비밀번호로 로그인하고 JWT 토큰(Access, Refresh)을 발급받습니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "email": "user@example.com",
          "password": "password123"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Headers`:
        -   `Authorization`: `Bearer <AccessToken>`
        -   `Refresh-Token`: `Bearer <RefreshToken>`
-   **Axios 예시**
    ```javascript
    // 로그인 요청
    const login = async (email, password) => {
      try {
        const response = await axios.post('/api/auth/login', { email, password });
        const accessToken = response.headers.authorization;
        const refreshToken = response.headers['refresh-token'];

        // 받은 토큰을 로컬 스토리지나 상태 관리에 저장
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        console.log('로그인 성공!');
      } catch (error) {
        console.error('로그인 실패:', error.response.data);
      }
    };
    ```

---

## 👤 2. 사용자 (User)

### 2.1. `P-201-SEARCH`: 상품 검색

-   **API**: `GET /api/products/search`
-   **설명**: 키워드, 카테고리, 호환성 등 다양한 조건으로 상품을 검색합니다.
-   **권한**: 없음 (로그인 시 호환성 필터 사용 가능)
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `keyword` (string, optional): 검색어
        -   `category` (string, optional): `CPU`, `GPU` 등
        -   `compatFilter` (boolean, optional): `true`로 설정 시, 로그인한 유저의 장바구니와 호환되는 부품만 검색
        -   `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<ProductSimpleResponse>`
        ```json
        {
          "content": [
            {
              "productId": 101,
              "productName": "[A판매자] Intel Core i5-13600K",
              "price": 350000.00,
              "imageUrl": "/images/some-image.jpg"
            }
          ],
          "pageable": { ... },
          "totalPages": 5,
          "totalElements": 48,
          ...
        }
        ```
-   **Axios 예시**
    ```javascript
    // 상품 검색 (호환성 필터 활성화)
    const searchProducts = async (params) => {
      try {
        // params = { keyword: 'i5', category: 'CPU', compatFilter: true, page: 0, size: 10 }
        const response = await axios.get('/api/products/search', { params });
        console.log('검색 결과:', response.data);
        return response.data;
      } catch (error) {
        console.error('상품 검색 실패:', error.response.data);
      }
    };
    ```

### 2.2. `[신규]` 카테고리 목록 조회

-   **API**: `GET /api/products/categories`
-   **설명**: 검색 페이지 등에서 사용할 전체 부품 카테고리 목록을 동적으로 조회합니다.
-   **권한**: 없음
-   **RData (Request Data)**: 없음
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<String>`
        ```json
        [
            "CPU",
            "Motherboard",
            "RAM",
            "GPU",
            "Storage",
            "Cooler",
            "Case",
            "Power"
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 전체 카테고리 목록 가져오기
    const fetchCategories = async () => {
      try {
        const response = await axios.get('/api/products/categories');
        return response.data; // ["CPU", "Motherboard", ...]
      } catch (error) {
        console.error('카테고리 조회 실패:', error.response.data);
        return [];
      }
    };
    ```

### 2.3. `[신규]` 동적 필터 옵션 조회

-   **API**: `GET /api/products/filters`
-   **설명**: 특정 카테고리에 대한 상세 필터 옵션 목록을 동적으로 조회합니다. (예: CPU 카테고리의 소켓 목록)
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `category` (string, required): `CPU`, `GPU` 등 카테고리명
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Map<String, Set<String>>`
    -   **CPU 요청 예시 (`?category=CPU`)**
        ```json
        {
            "socket": ["LGA1700", "AM5", "AM4", ...],
            "supportedMemoryTypes": ["DDR5", "DDR4", "DDR3"]
        }
        ```
    -   **GPU 요청 예시 (`?category=GPU`)**
        ```json
        {
            "pcieVersion": ["5.0", "4.0", "3.0"]
        }
        ```
-   **Axios 예시**
    ```javascript
    // CPU 카테고리의 필터 옵션 가져오기
    const fetchFilterOptions = async (category) => {
      try {
        const response = await axios.get('/api/products/filters', {
          params: { category }
        });
        return response.data; // { socket: [...], ... }
      } catch (error) {
        console.error(`${category} 필터 옵션 조회 실패:`, error.response.data);
        return {};
      }
    };
    ```

### 4.4. `P-401`: 주문 상세 조회

-   **API**: `GET /api/orders/{orderId}`
-   **설명**: 특정 주문의 상세 내역(주문 상품, 배송지 정보 등)을 조회합니다.
-   **권한**: `USER` (자신의 주문만 조회 가능)
-   **RData (Request Data)**
    -   `Path Parameter`: `orderId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `OrderDetailResponseDto`
-   **Axios 예시**
    ```javascript
    const getOrderDetail = async (orderId) => {
      try {
        const response = await axios.get(`/api/orders/${orderId}`, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('주문 상세 조회 실패:', error.response.data);
      }
    };
    ```

---

## 🧠 5. 추천 및 로깅 (Recommendation & Logging)

### 5.1. `U-401`: 개인화 추천 요청

-   **API**: `GET /api/recommendations/personal`
-   **설명**: 사용자의 행동 로그와 장바구니 상태를 기반으로 개인화된 상품을 추천받습니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `eventType` (string, optional, default: `detail-page-view`): 추천을 요청하는 페이지의 컨텍스트
        -   `count` (int, optional, default: `5`): 받고자 하는 추천 상품 개수
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<RecommendationResponseDto>`
        ```json
        [
          {
            "product": { ... }, // ProductSimpleResponse
            "compatibilityStatus": "SUCCESS",
            "compatibilityMessages": []
          }
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 개인화 추천 상품 요청
    const getPersonalRecommendations = async (count = 5) => {
      try {
        const response = await axios.get('/api/recommendations/personal', {
          params: { count },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('추천 상품:', response.data);
        return response.data;
      } catch (error) {
        console.error('추천 요청 실패:', error.response.data);
      }
    };
    ```

### 5.2. `SYS-3`: 프론트엔드 행동 로깅

-   **API**: `POST /api/logs/action`
-   **설명**: 사용자의 주요 행동(상세 페이지 오래 보기, 이미지 클릭 등)을 서버에 기록하여 추천 엔진의 의도 점수를 갱신합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "baseSpecId": "base_cpu_intel_intel-core-i9-12900k_bc573357",
          "actionType": "LONG_VIEW" // VIEW, LONG_VIEW, IMAGE_VIEW 등
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 15초 이상 머무를 때 'LONG_VIEW' 로그 전송
    const logLongView = (baseSpecId) => {
      axios.post('/api/logs/action', { baseSpecId, actionType: 'LONG_VIEW' }, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      }).catch(err => console.error('로그 전송 실패', err));
    };
    ```

---

## 📁 6. 파일 (File)

### 6.1. `P-601`: 파일 업로드

-   **API**: `POST /api/files/upload`
-   **설명**: 상품/모델 등록/수정 시 이미지를 먼저 서버에 업로드하고, 반환된 파일명을 `imageUrl` 필드에 담아 전송합니다.
-   **권한**: `SELLER`, `ADMIN`
-   **RData (Request Data)**
    -   `Body (multipart/form-data)`: `file` 키로 이미지 파일 전송
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`:
        ```json
        {
          "fileName": "generated_unique_filename.jpg",
          "fileUrl": "http://localhost:8080/images/generated_unique_filename.jpg"
        }
        ```
-   **Axios 예시**
    ```javascript
    // 이미지 파일 업로드
    const uploadImage = async (file) => {
      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await axios.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': localStorage.getItem('accessToken')
          }
        });
        console.log('업로드 성공:', response.data);
        return response.data; // { fileName: '...', fileUrl: '...' }
      } catch (error) {
        console.error('파일 업로드 실패:', error.response.data);
      }
    };
    ```

---

## 🏬 7. 판매자 (Seller)

### 7.1. 상품 관리

#### `S-201`: 기반 모델 검색
-   **API**: `GET /api/seller/base-specs`
-   **설명**: 판매자가 자신의 상품을 등록하기 전, 시스템에 등록된 기반 모델(`BaseSpec`)을 검색합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `query` (string, required) - 검색할 모델명 키워드
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<BaseSpecSearchResponse>`
        ```json
        [
          {
            "baseSpecId": "base_cpu_intel_intel-core-i5-13600k_...",
            "name": "Intel Core i5-13600K"
          }
        ]
        ```
-   **Axios 예시**
    ```javascript
    const searchBaseSpecsForSeller = async (keyword) => {
      try {
        const response = await axios.get('/api/seller/base-specs', {
          params: { query: keyword },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('기반 모델 검색 실패:', error.response.data);
      }
    };
    ```

#### `S-201.2`: 신규 기반 모델 등록 요청
-   **API**: `POST /api/seller/base-spec-requests`
-   **설명**: 검색으로 찾을 수 없는 모델에 대해 카테고리, 제조사, 모델명을 포함하여 Admin에게 등록을 요청합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "requestedModelName": "Nvidia RTX 5090",
          "category": "GPU",
          "manufacturer": "NVIDIA"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 요청의 `requestId` (long)
-   **Axios 예시**
    ```javascript
    const requestNewBaseSpec = async (requestData) => {
      const response = await axios.post('/api/seller/base-spec-requests', requestData, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      });
      console.log('모델 등록 요청 성공! Request ID:', response.data);
    };
    ```

#### `S-201.3`: 판매 상품 등록
-   **API**: `POST /api/seller/products`
-   **설명**: 검색된 `BaseSpec`에 자신의 판매 정보를 연결하여 새로운 상품을 등록합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "baseSpecId": "base_cpu_intel_intel-core-i5-13600k_...",
          "name": "[특가] 인텔 코어 i5-13600K 정품",
          "price": 350000.00,
          "stockQuantity": 100,
          "imageUrl": "some_unique_filename.jpg" // 파일 업로드 API로 받은 파일명
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 상품의 `productId` (long)
-   **Axios 예시**
    ```javascript
    const createSellerProduct = async (productData) => {
      try {
        const response = await axios.post('/api/seller/products', productData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 등록 성공! Product ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('상품 등록 실패:', error.response.data);
      }
    };
    ```

#### `S-202`: 내 판매 상품 목록 조회
-   **API**: `GET /api/seller/products`
-   **설명**: 현재 로그인한 판매자가 등록한 모든 상품 목록을 페이징하여 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<SellerProductListResponse>`
-   **Axios 예시**
    ```javascript
    const getMySellerProducts = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/seller/products', {
          params: { page, size },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('내 판매 상품 조회 실패:', error.response.data);
      }
    };
    ```

#### `S-203`: 내 판매 상품 수정을 위한 정보 조회
-   **API**: `GET /api/seller/products/{productId}/edit`
-   **설명**: 상품 수정 페이지를 채우기 위해, 특정 상품의 현재 정보를 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `ProductUpdateFormResponse`
-   **Axios 예시**
    ```javascript
    const getProductForUpdate = async (productId) => {
      const response = await axios.get(`/api/seller/products/${productId}/edit`, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      });
      return response.data; // 수정 폼에 채울 데이터
    };
    ```

#### `S-203`: 내 판매 상품 수정
-   **API**: `PUT /api/seller/products/{productId}`
-   **설명**: 자신의 판매 상품 정보(가격, 재고 등)를 수정합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
    -   `Body (application/json)`
        ```json
        {
          "name": "[긴급할인] 인텔 코어 i5-13600K 정품",
          "price": 345000.00,
          "stockQuantity": 50
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateSellerProduct = async (productId, updateData) => {
      try {
        await axios.put(`/api/seller/products/${productId}`, updateData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 수정 성공!');
      } catch (error) {
        console.error('상품 수정 실패:', error.response.data);
      }
    };
    ```

#### `S-204`: 내 판매 상품 삭제
-   **API**: `DELETE /api/seller/products/{productId}`
-   **설명**: 자신의 판매 상품을 삭제(Soft Delete)합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    const deleteSellerProduct = async (productId) => {
      try {
        await axios.delete(`/api/seller/products/${productId}`, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 삭제 성공!');
      } catch (error) {
        console.error('상품 삭제 실패:', error.response.data);
      }
    };
    ```

### 7.2. 주문 관리

#### `S-301`: 내 상품에 대한 신규 주문 목록 조회
-   **API**: `GET /api/seller/orders`
-   **설명**: 자신의 상품이 포함된 주문 내역을 페이징하여 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<SellerOrderResponseDto>`
-   **Axios 예시**
    ```javascript
    const getMySalesOrders = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/seller/orders', {
          params: { page, size },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('판매 주문 조회 실패:', error.response.data);
      }
    };
    ```

#### `S-302`: 배송 상태 변경 및 송장 번호 입력
-   **API**: `PUT /api/seller/orders/{orderItemId}`
-   **설명**: 특정 주문 항목의 배송 상태를 변경하고 송장 번호를 입력합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `orderItemId` (long)
    -   `Body (application/json)`
        ```json
        {
          "status": "SHIPPED", // PREPARING, SHIPPED, DELIVERED 등
          "trackingNumber": "1234567890"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateOrderItemStatus = async (orderItemId, status, trackingNumber) => {
      try {
        await axios.put(`/api/seller/orders/${orderItemId}`, { status, trackingNumber }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('배송 상태 업데이트 성공!');
      } catch (error) {
        console.error('배송 상태 업데이트 실패:', error.response.data);
      }
    };
    ```

---

## 🛠️ 8. 관리자 (Admin)

### 8.1. 회원/판매자 관리

#### `A-101`: 판매자 가입 요청 목록 조회
-   **API**: `GET /api/admin/seller-requests`
-   **설명**: 가입을 요청한 판매자(`SELLER_PENDING`) 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<SellerRequestDto>`

#### `A-101`: 판매자 가입 승인
-   **API**: `PUT /api/admin/seller-requests/{userId}/approve`
-   **설명**: 특정 판매자의 가입 요청을 승인하고 `SELLER` 역할로 변경합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `userId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

#### `A-102`: 전체 회원 목록 조회
-   **API**: `GET /api/admin/users`
-   **설명**: 모든 사용자(USER, SELLER 등) 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<UserManagementResponse>`

#### `A-102`: 회원 상태 변경
-   **API**: `PUT /api/admin/users/{userId}/status`
-   **설명**: 특정 사용자의 계정 상태(`ACTIVE`, `SUSPENDED` 등)를 변경합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `userId` (long)
    -   `Body (application/json)`: `{"status": "SUSPENDED"}`
-   **SData (Success Data)**
    -   `Status`: `200 OK`

### 8.2. 기반 모델(부품) 관리

#### `A-201-ADD`: 신규 기반 모델 등록
-   **API**: `POST /api/admin/parts`
-   **설명**: 새로운 부품의 기반 모델(`BaseSpec`)과 상세 사양을 등록합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Body (application/json)`: `BaseSpecCreateRequest` (상세 내용은 DTO 참조)
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 `baseSpecId` (string)

#### `A-201-LIST`: 기반 모델 목록 조회
-   **API**: `GET /api/admin/parts`
-   **설명**: 시스템에 등록된 모든 기반 모델을 검색 조건과 함께 페이징하여 조회합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Query Parameters`: `keyword`, `category` 등 `BaseSpecSearchCondition` DTO 참조
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<BaseSpecListResponse>`

#### `A-202`: 기반 모델 상세 조회
-   **API**: `GET /api/admin/parts/{baseSpecId}`
-   **설명**: 특정 기반 모델의 상세 정보를 조회합니다. (수정 폼 채우기용)
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `baseSpecId` (string)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `BaseSpecDetailResponse`

#### `A-202`: 기반 모델 수정
-   **API**: `PUT /api/admin/parts/{baseSpecId}`
-   **설명**: 특정 기반 모델의 정보를 수정합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `baseSpecId` (string)
    -   `Body (application/json)`: `BaseSpecUpdateRequest` (상세 내용은 DTO 참조)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

#### `A-203`: 판매자의 모델 등록 요청 목록 조회
-   **API**: `GET /api/admin/base-spec-requests`
-   **설명**: 판매자들이 요청한 신규 기반 모델 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<BaseSpecRequestResponseDto>`

#### `A-203`: 판매자의 모델 등록 요청 처리
-   **API**: `PUT /api/admin/base-spec-requests/{requestId}`
-   **설명**: 판매자의 모델 등록 요청을 승인(`APPROVED`) 또는 거절(`REJECTED`)합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `requestId` (long)
    -   `Body (application/json)`: `{"status": "APPROVED", "adminNotes": "처리 완료"}`
-   **SData (Success Data)**
    -   `Status`: `200 OK`

### 8.3. 시스템 관리 및 테스트

#### `A-401`: 텍스트 파일로 데이터 초기화
-   **API**: `POST /api/admin/parts/initialize-from-file`
-   **설명**: 서버에 위치한 `basespec.txt` 파일을 읽어 대량의 `BaseSpec` 데이터를 DB에 초기화합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: "Data initialization from basespec.txt has been triggered."

#### `A-401`: 테스트용 상품 대량 생성
-   **API**: `POST /api/admin/parts/generate-products`
-   **설명**: 모든 `BaseSpec`에 대해 특정 판매자의 `Product`를 지정된 개수만큼 대량으로 생성합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `sellerId` (long, required)
        -   `count` (int, optional, default: 10)
        -   `imageUrl` (string, required)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

#### `SYS-3`: AI 카탈로그 동기화
-   **API**: `POST /api/admin/sync/catalog`
-   **설명**: 로컬 DB의 모든 `BaseSpec` 데이터를 Google Cloud Retail AI의 카탈로그와 동기화합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 동기화 결과 요약 문자열

---

## 👤 9. 사용자 프로필 (User Profile)

### 9.1. `U-504`: 내 정보 조회
-   **API**: `GET /api/user/me`
-   **설명**: 현재 로그인한 사용자의 정보를 조회합니다.
-   **권한**: `USER`, `SELLER`, `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `UserInfoResponse`
        ```json
        {
          "email": "user@example.com",
          "username": "SOMA_USER",
          "role": "USER"
        }
        ```
-   **Axios 예시**
    ```javascript
    const getMyInfo = async () => {
      try {
        const response = await axios.get('/api/user/me', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('내 정보 조회 실패:', error.response.data);
      }
    };
    ```

### 9.2. `U-504`: 내 정보 수정
-   **API**: `PUT /api/user/me`
-   **설명**: 현재 로그인한 사용자의 닉네임, 비밀번호 등을 수정합니다.
-   **권한**: `USER`, `SELLER`, `ADMIN`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "username": "NEW_SOMA_USER",
          "currentPassword": "password123",
          "newPassword": "newPassword456"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateMyInfo = async (updateData) => {
      try {
        await axios.put('/api/user/me', updateData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('정보 수정 성공!');
      } catch (error) {
        console.error('정보 수정 실패:', error.response.data);
      }
    };
    ```

### 9.3. `U-505`: 회원 탈퇴
-   **API**: `DELETE /api/user/me`
-   **설명**: 현재 로그인한 사용자의 계정을 비활성화(Soft Delete)합니다.
-   **권한**: `USER`, `SELLER`
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    const deactivateMyAccount = async () => {
      try {
        await axios.delete('/api/user/me', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        // 성공 시 로컬 스토리지의 토큰을 삭제하고 로그아웃 처리
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        console.log('회원 탈퇴 성공!');
      } catch (error) {
        console.error('회원 탈퇴 실패:', error.response.data);
      }
    };
    ```

---

## 🧪 10. 테스트 전용 (Test Only)

> **주의**: 이 섹션의 API들은 개발 및 테스트 목적으로만 사용되며, 실제 운영 환경에서는 비활성화되거나 제거될 수 있습니다.

### 10.1. `test/1-import-catalog`: AI 카탈로그 저장 (테스트 데이터)
-   **API**: `GET /test/1-import-catalog`
-   **설명**: `RecommendationTestService`에 하드코딩된 101개의 샘플 제품을 Google Cloud 카탈로그에 전송합니다.
-   **권한**: 없음

### 10.2. `test/2-ingest-logs`: AI 로그 저장 (테스트 데이터)
-   **API**: `GET /test/2-ingest-logs`
-   **설명**: `user_001`에 대한 6건의 하드코딩된 행동 로그를 Google Cloud에 전송합니다.
-   **권한**: 없음

### 10.3. `test/3-get-recommendations`: AI 추천 요청 (FBT 모델)
-   **API**: `GET /test/3-get-recommendations`
-   **설명**: "자주 함께 구매하는 항목" 모델을 테스트합니다.
-   **권한**: 없음

### 10.4. `test/3-get-similar-items`: AI 추천 요청 (유사 품목 모델)
-   **API**: `GET /test/3-get-similar-items`
-   **설명**: "유사 품목" 모델을 테스트합니다.
-   **권한**: 없음

### 2.2. `P-201.1`: 검색 자동완성

-   **API**: `GET /api/products/autocomplete`
-   **설명**: 검색창에 입력 중인 키워드에 대한 자동완성 추천 목록을 제공합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `keyword` (string, required): 2글자 이상의 검색어
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<AutocompleteResponse>`
        ```json
        [
          { "name": "Intel Core i5-13600K" },
          { "name": "Intel Core i5-12400F" }
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 자동완성 제안 요청
    const fetchAutocomplete = async (keyword) => {
      if (keyword.length < 2) return [];
      try {
        const response = await axios.get('/api/products/autocomplete', { params: { keyword } });
        return response.data; // [{name: '...'}, ...]
      } catch (error) {
        console.error('자동완성 조회 실패:', error.response.data);
        return [];
      }
    };
    ```

### 2.3. `P-202`: 상품 상세 조회

-   **API**: `GET /api/products/{productId}`
-   **설명**: 특정 상품의 상세 정보, 기술 사양, 가격 비교 목록 등을 조회합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `ProductDetailResponse` (매우 복잡한 객체, 주요 필드만 예시)
        ```json
        {
          "product": {
            "productId": 101,
            "productName": "[A판매자] Intel Core i5-13600K",
            "price": 350000.00,
            "imageUrl": "/images/some-image.jpg",
            "stock": 50
          },
          "baseSpec": {
            "name": "Intel Core i5-13600K",
            "manufacturer": "Intel",
            "category": "CPU",
            "cpuSpec": {
              "socket": "LGA1700",
              "supportedMemoryTypes": ["DDR5", "DDR4"],
              "hasIgpu": true
            }
          },
          "sellerInfo": { ... },
          "priceComparison": [ ... ]
        }
        ```
-   **Axios 예시**
    ```javascript
    // 상품 상세 정보 조회
    const getProductDetail = async (productId) => {
      try {
        const response = await axios.get(`/api/products/${productId}`);
        console.log('상품 상세 정보:', response.data);
        return response.data;
      } catch (error) {
        console.error('상품 상세 조회 실패:', error.response.data);
      }
    };
    ```

---

## 🛒 3. 장바구니 (Cart)

### 3.1. `P-301`: 장바구니에 상품 추가

-   **API**: `POST /api/cart/items`
-   **설명**: 특정 상품을 지정된 수량만큼 장바구니에 추가합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "productId": 101,
          "quantity": 1
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 장바구니에 상품 추가 (반드시 인증 토큰 필요)
    const addToCart = async (productId, quantity) => {
      try {
        await axios.post('/api/cart/items', { productId, quantity }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('장바구니 추가 성공!');
      } catch (error) {
        console.error('장바구니 추가 실패:', error.response.data);
      }
    };
    ```

### 3.2. `P-301`: 내 장바구니 조회

-   **API**: `GET /api/cart`
-   **설명**: 현재 로그인한 사용자의 장바구니 목록과 전체 견적의 호환성 검사 결과를 조회합니다.
-   **권한**: `USER`
-   **RData (Request Data)**: 없음
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `CartResponse`
        ```json
        {
          "cartItems": [
            {
              "cartItemId": 1,
              "product": { "productId": 101, "productName": "...", ... },
              "quantity": 1
            }
          ],
          "totalPrice": 350000.00,
          "compatibilityResult": {
            "status": "SUCCESS", // SUCCESS, WARN, FAIL
            "messages": []
          }
        }
        ```
-   **Axios 예시**
    ```javascript
    // 내 장바구니 조회
    const getMyCart = async () => {
      try {
        const response = await axios.get('/api/cart', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('장바구니 정보:', response.data);
        return response.data;
      } catch (error) {
        console.error('장바구니 조회 실패:', error.response.data);
      }
    };
    ```

### 3.3. `P-301`: 장바구니 상품 수량 수정

-   **API**: `PUT /api/cart/items/{cartItemId}`
-   **설명**: 장바구니에 담긴 특정 아이템의 수량을 변경합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Path Parameter`: `cartItemId` (long)
    -   `Body (application/json)`
        ```json
        { "quantity": 2 }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 장바구니 상품 수량 변경
    const updateCartItemQuantity = async (cartItemId, newQuantity) => {
      try {
        await axios.put(`/api/cart/items/${cartItemId}`, { quantity: newQuantity }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('수량 변경 성공!');
      } catch (error) {
        console.error('수량 변경 실패:', error.response.data);
      }
    };
    ```

### 3.4. `U-301.5`: 장바구니 상품 선택 삭제

-   **API**: `DELETE /api/cart/items`
-   **설명**: 장바구니에서 여러 상품을 한 번에 삭제합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "cartItemIds": [1, 2, 3]
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    // 장바구니 선택 삭제
    const deleteCartItems = async (cartItemIds) => {
      try {
        await axios.delete('/api/cart/items', {
          headers: { 'Authorization': localStorage.getItem('accessToken') },
          data: { cartItemIds } // DELETE 요청 시 body는 data 속성에 담아야 함
        });
        console.log('선택 삭제 성공!');
      } catch (error) {
        console.error('선택 삭제 실패:', error.response.data);
      }
    };
    ```

---

## 🧾 4. 주문 (Order)

### 4.1. `P-501`: 주문 생성 (장바구니 기반)

-   **API**: `POST /api/orders`
-   **설명**: 장바구니에 담긴 모든 상품으로 주문을 생성합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "recipientName": "홍길동",
          "shippingAddress": "서울시 강남구 테헤란로",
          "shippingPostcode": "06123"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 생성된 주문의 `orderId` (long)
-   **Axios 예시**
    ```javascript
    // 장바구니 상품으로 주문하기
    const createOrderFromCart = async (shippingInfo) => {
      try {
        const response = await axios.post('/api/orders', shippingInfo, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('주문 성공! Order ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('주문 실패:', error.response.data);
      }
    };
    ```

### 4.2. `P-202`: 즉시 구매

-   **API**: `POST /api/orders/instant`
-   **설명**: 단일 상품을 즉시 구매하는 주문을 생성합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "productId": 101,
          "quantity": 1,
          "recipientName": "홍길동",
          "shippingAddress": "서울시 강남구 테헤란로",
          "shippingPostcode": "06123"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 생성된 주문의 `orderId` (long)
-   **Axios 예시**
    ```javascript
    // 즉시 구매하기
    const createInstantOrder = async (orderInfo) => {
      try {
        const response = await axios.post('/api/orders/instant', orderInfo, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('즉시 구매 성공! Order ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('즉시 구매 실패:', error.response.data);
      }
    };
    ```

### 4.3. `P-401`: 주문 내역 조회

-   **API**: `GET /api/orders`
-   **설명**: 로그인한 사용자의 주문 내역 목록을 페이징하여 조회합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<OrderListResponseDto>`
-   **Axios 예시**
    ```javascript
    // 내 주문 내역 조회
    const getMyOrders = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/orders', {
          params: { page, size, sort: 'orderedAt,desc' },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('주문 내역 조회 실패:', error.response.data);
      }
    };
    ```

---

## 🧠 5. 추천 및 로깅 (Recommendation & Logging)

### 5.1. `U-401`: 개인화 추천 요청

-   **API**: `GET /api/recommendations/personal`
-   **설명**: 사용자의 행동 로그와 장바구니 상태를 기반으로 개인화된 상품을 추천받습니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `eventType` (string, optional, default: `detail-page-view`): 추천을 요청하는 페이지의 컨텍스트
        -   `count` (int, optional, default: `5`): 받고자 하는 추천 상품 개수
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<RecommendationResponseDto>`
        ```json
        [
          {
            "product": { ... }, // ProductSimpleResponse
            "compatibilityStatus": "SUCCESS",
            "compatibilityMessages": []
          }
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 개인화 추천 상품 요청
    const getPersonalRecommendations = async (count = 5) => {
      try {
        const response = await axios.get('/api/recommendations/personal', {
          params: { count },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('추천 상품:', response.data);
        return response.data;
      } catch (error) {
        console.error('추천 요청 실패:', error.response.data);
      }
    };
    ```

### 5.2. `SYS-3`: 프론트엔드 행동 로깅

-   **API**: `POST /api/logs/action`
-   **설명**: 사용자의 주요 행동(상세 페이지 오래 보기, 이미지 클릭 등)을 서버에 기록하여 추천 엔진의 의도 점수를 갱신합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "baseSpecId": "base_cpu_intel_intel-core-i9-12900k_bc573357",
          "actionType": "LONG_VIEW" // VIEW, LONG_VIEW, IMAGE_VIEW 등
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 15초 이상 머무를 때 'LONG_VIEW' 로그 전송
    const logLongView = (baseSpecId) => {
      axios.post('/api/logs/action', { baseSpecId, actionType: 'LONG_VIEW' }, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      }).catch(err => console.error('로그 전송 실패', err));
    };
    ```

---

## 📁 6. 파일 (File)

### 6.1. `P-601`: 파일 업로드

-   **API**: `POST /api/files/upload`
-   **설명**: 상품/모델 등록/수정 시 이미지를 먼저 서버에 업로드하고, 반환된 파일명을 `imageUrl` 필드에 담아 전송합니다.
-   **권한**: `SELLER`, `ADMIN`
-   **RData (Request Data)**
    -   `Body (multipart/form-data)`: `file` 키로 이미지 파일 전송
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`:
        ```json
        {
          "fileName": "generated_unique_filename.jpg",
          "fileUrl": "http://localhost:8080/images/generated_unique_filename.jpg"
        }
        ```
-   **Axios 예시**
    ```javascript
    // 이미지 파일 업로드
    const uploadImage = async (file) => {
      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await axios.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': localStorage.getItem('accessToken')
          }
        });
        console.log('업로드 성공:', response.data);
        return response.data; // { fileName: '...', fileUrl: '...' }
      } catch (error) {
        console.error('파일 업로드 실패:', error.response.data);
      }
    };
    ```

---

## 🏬 3. 판매자 (Seller)

### 3.1. 상품 관리

#### `S-201`: 기반 모델 검색
-   **API**: `GET /api/seller/base-specs`
-   **설명**: 판매자가 자신의 상품을 등록하기 전, 시스템에 등록된 기반 모델(`BaseSpec`)을 검색합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `query` (string, required) - 검색할 모델명 키워드
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<BaseSpecSearchResponse>`
        ```json
        [
          {
            "baseSpecId": "base_cpu_intel_intel-core-i5-13600k_...",
            "name": "Intel Core i5-13600K"
          }
        ]
        ```
-   **Axios 예시**
    ```javascript
    const searchBaseSpecsForSeller = async (keyword) => {
      try {
        const response = await axios.get('/api/seller/base-specs', {
          params: { query: keyword },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('기반 모델 검색 실패:', error.response.data);
      }
    };
    ```

#### `S-201.3`: 판매 상품 등록
-   **API**: `POST /api/seller/products`
-   **설명**: 검색된 `BaseSpec`에 자신의 판매 정보를 연결하여 새로운 상품을 등록합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "baseSpecId": "base_cpu_intel_intel-core-i5-13600k_...",
          "name": "[특가] 인텔 코어 i5-13600K 정품",
          "price": 350000.00,
          "stockQuantity": 100,
          "imageUrl": "some_unique_filename.jpg" // 파일 업로드 API로 받은 파일명
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 상품의 `productId` (long)
-   **Axios 예시**
    ```javascript
    const createSellerProduct = async (productData) => {
      try {
        const response = await axios.post('/api/seller/products', productData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 등록 성공! Product ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('상품 등록 실패:', error.response.data);
      }
    };
    ```

#### `S-202`: 내 판매 상품 목록 조회
-   **API**: `GET /api/seller/products`
-   **설명**: 현재 로그인한 판매자가 등록한 모든 상품 목록을 페이징하여 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<SellerProductListResponse>`
-   **Axios 예시**
    ```javascript
    const getMySellerProducts = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/seller/products', {
          params: { page, size },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('내 판매 상품 조회 실패:', error.response.data);
      }
    };
    ```

#### `S-203`: 내 판매 상품 수정
-   **API**: `PUT /api/seller/products/{productId}`
-   **설명**: 자신의 판매 상품 정보(가격, 재고 등)를 수정합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
    -   `Body (application/json)`
        ```json
        {
          "name": "[긴급할인] 인텔 코어 i5-13600K 정품",
          "price": 345000.00,
          "stockQuantity": 50
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateSellerProduct = async (productId, updateData) => {
      try {
        await axios.put(`/api/seller/products/${productId}`, updateData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 수정 성공!');
      } catch (error) {
        console.error('상품 수정 실패:', error.response.data);
      }
    };
    ```

#### `S-204`: 내 판매 상품 삭제
-   **API**: `DELETE /api/seller/products/{productId}`
-   **설명**: 자신의 판매 상품을 삭제(Soft Delete)합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    const deleteSellerProduct = async (productId) => {
      try {
        await axios.delete(`/api/seller/products/${productId}`, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('상품 삭제 성공!');
      } catch (error) {
        console.error('상품 삭제 실패:', error.response.data);
      }
    };
    ```

#### `S-201.2`: 신규 기반 모델 등록 요청
-   **API**: `POST /api/seller/base-spec-requests`
-   **설명**: 검색으로 찾을 수 없는 모델에 대해 카테고리, 제조사, 모델명을 포함하여 Admin에게 등록을 요청합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "requestedModelName": "Nvidia RTX 5090",
          "category": "GPU",
          "manufacturer": "NVIDIA"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 요청의 `requestId` (long)
-   **Axios 예시**
    ```javascript
    const requestNewBaseSpec = async (requestData) => {
      const response = await axios.post('/api/seller/base-spec-requests', requestData, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      });
      console.log('모델 등록 요청 성공! Request ID:', response.data);
    };
    ```

### 3.2. 주문 관리

#### `S-301`: 내 상품에 대한 신규 주문 목록 조회
-   **API**: `GET /api/seller/orders`
-   **설명**: 자신의 상품이 포함된 주문 내역을 페이징하여 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<SellerOrderResponseDto>`
-   **Axios 예시**
    ```javascript
    const getMySalesOrders = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/seller/orders', {
          params: { page, size },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('판매 주문 조회 실패:', error.response.data);
      }
    };
    ```

#### `S-302`: 배송 상태 변경 및 송장 번호 입력
-   **API**: `PUT /api/seller/orders/{orderItemId}`
-   **설명**: 특정 주문 항목의 배송 상태를 변경하고 송장 번호를 입력합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `orderItemId` (long)
    -   `Body (application/json)`
        ```json
        {
          "status": "SHIPPED", // PREPARING, SHIPPED, DELIVERED 등
          "trackingNumber": "1234567890"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateOrderItemStatus = async (orderItemId, status, trackingNumber) => {
      try {
        await axios.put(`/api/seller/orders/${orderItemId}`, { status, trackingNumber }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('배송 상태 업데이트 성공!');
      } catch (error) {
        console.error('배송 상태 업데이트 실패:', error.response.data);
      }
    };
    ```

#### `S-203`: 내 판매 상품 수정을 위한 정보 조회
-   **API**: `GET /api/seller/products/{productId}/edit`
-   **설명**: 상품 수정 페이지를 채우기 위해, 특정 상품의 현재 정보를 조회합니다.
-   **권한**: `SELLER`
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `ProductUpdateFormResponse`
-   **Axios 예시**
    ```javascript
    const getProductForUpdate = async (productId) => {
      const response = await axios.get(`/api/seller/products/${productId}/edit`, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      });
      return response.data; // 수정 폼에 채울 데이터
    };
    ```

---

## 🛠️ 4. 관리자 (Admin)

### 4.1. 회원/판매자 관리

#### `A-101`: 판매자 가입 요청 목록 조회
-   **API**: `GET /api/admin/seller-requests`
-   **설명**: 가입을 요청한 판매자(`SELLER_PENDING`) 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<SellerRequestDto>`

#### `A-101`: 판매자 가입 승인
-   **API**: `PUT /api/admin/seller-requests/{userId}/approve`
-   **설명**: 특정 판매자의 가입 요청을 승인하고 `SELLER` 역할로 변경합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `userId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

#### `A-102`: 전체 회원 목록 조회
-   **API**: `GET /api/admin/users`
-   **설명**: 모든 사용자(USER, SELLER 등) 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<UserManagementResponse>`

#### `A-102`: 회원 상태 변경
-   **API**: `PUT /api/admin/users/{userId}/status`
-   **설명**: 특정 사용자의 계정 상태(`ACTIVE`, `SUSPENDED` 등)를 변경합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `userId` (long)
    -   `Body (application/json)`: `{"status": "SUSPENDED"}`
-   **SData (Success Data)**
    -   `Status`: `200 OK`

### 4.2. 기반 모델(부품) 관리

#### `A-201-ADD`: 신규 기반 모델 등록
-   **API**: `POST /api/admin/parts`
-   **설명**: 새로운 부품의 기반 모델(`BaseSpec`)과 상세 사양을 등록합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Body (application/json)`: `BaseSpecCreateRequest` (상세 내용은 DTO 참조)
-   **SData (Success Data)**
    -   `Status`: `201 Created`
    -   `Body`: 생성된 `baseSpecId` (string)

#### `A-201-LIST`: 기반 모델 목록 조회
-   **API**: `GET /api/admin/parts`
-   **설명**: 시스템에 등록된 모든 기반 모델을 검색 조건과 함께 페이징하여 조회합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Query Parameters`: `keyword`, `category` 등 `BaseSpecSearchCondition` DTO 참조
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<BaseSpecListResponse>`

#### `A-202`: 기반 모델 상세 조회
-   **API**: `GET /api/admin/parts/{baseSpecId}`
-   **설명**: 특정 기반 모델의 상세 정보를 조회합니다. (수정 폼 채우기용)
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `baseSpecId` (string)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `BaseSpecDetailResponse`

#### `A-202`: 기반 모델 수정
-   **API**: `PUT /api/admin/parts/{baseSpecId}`
-   **설명**: 특정 기반 모델의 정보를 수정합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `baseSpecId` (string)
    -   `Body (application/json)`: `BaseSpecUpdateRequest` (상세 내용은 DTO 참조)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

#### `A-203`: 판매자의 모델 등록 요청 목록 조회
-   **API**: `GET /api/admin/base-spec-requests`
-   **설명**: 판매자들이 요청한 신규 기반 모델 목록을 조회합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<BaseSpecRequestResponseDto>`

#### `A-203`: 판매자의 모델 등록 요청 처리
-   **API**: `PUT /api/admin/base-spec-requests/{requestId}`
-   **설명**: 판매자의 모델 등록 요청을 승인(`APPROVED`) 또는 거절(`REJECTED`)합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Path Parameter`: `requestId` (long)
    -   `Body (application/json)`: `{"status": "APPROVED", "adminNotes": "처리 완료"}`
-   **SData (Success Data)**
    -   `Status`: `200 OK`

### 4.3. 테스트용 API

#### `A-401`: 테스트용 상품 대량 생성
-   **API**: `POST /api/admin/parts/generate-products`
-   **설명**: 모든 `BaseSpec`에 대해 특정 판매자의 `Product`를 지정된 개수만큼 대량으로 생성합니다.
-   **권한**: `ADMIN`
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `sellerId` (long, required)
        -   `count` (int, optional, default: 10)
        -   `imageUrl` (string, required)
-   **SData (Success Data)**
    -   `Status`: `200 OK`

### 4.3. 시스템 관리 및 테스트

#### `A-401`: 텍스트 파일로 데이터 초기화
-   **API**: `POST /api/admin/parts/initialize-from-file`
-   **설명**: 서버에 위치한 `basespec.txt` 파일을 읽어 대량의 `BaseSpec` 데이터를 DB에 초기화합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: "Data initialization from basespec.txt has been triggered."

#### `SYS-3`: AI 카탈로그 동기화
-   **API**: `POST /api/admin/sync/catalog`
-   **설명**: 로컬 DB의 모든 `BaseSpec` 데이터를 Google Cloud Retail AI의 카탈로그와 동기화합니다.
-   **권한**: `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 동기화 결과 요약 문자열

#### `SYS-3`: AI 모델 학습용 이벤트 전송 (테스트용)
-   **API**: `POST /api/recommendations/ingest-events`
-   **설명**: AI 추천 모델 학습을 위해 가상의 사용자 행동 로그(`detail-page-view`)를 대량으로 전송합니다.
-   **권한**: `ADMIN` (실제로는 ADMIN 권한 필요)
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "productIds": [
            "base_cpu_intel_intel-core-i9-12900k_...",
            "base_motherboard_asus_rog-strix-z790-e-gaming_..."
          ]
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 처리 결과 요약 문자열

---

## 🧪 5. 테스트 전용 (Test Only)

> **주의**: 이 섹션의 API들은 개발 및 테스트 목적으로만 사용되며, 실제 운영 환경에서는 비활성화되거나 제거될 수 있습니다.

### 5.1. `test/1-import-catalog`: AI 카탈로그 저장 (테스트 데이터)
-   **API**: `GET /test/1-import-catalog`
-   **설명**: `RecommendationTestService`에 하드코딩된 101개의 샘플 제품을 Google Cloud 카탈로그에 전송합니다.
-   **권한**: 없음

### 5.2. `test/2-ingest-logs`: AI 로그 저장 (테스트 데이터)
-   **API**: `GET /test/2-ingest-logs`
-   **설명**: `user_001`에 대한 6건의 하드코딩된 행동 로그를 Google Cloud에 전송합니다.
-   **권한**: 없음

### 5.3. `test/3-get-recommendations`: AI 추천 요청 (FBT 모델)
-   **API**: `GET /test/3-get-recommendations`
-   **설명**: "자주 함께 구매하는 항목" 모델을 테스트합니다.
-   **권한**: 없음

### 5.4. `test/3-get-similar-items`: AI 추천 요청 (유사 품목 모델)
-   **API**: `GET /test/3-get-similar-items`
-   **설명**: "유사 품목" 모델을 테스트합니다.
-   **권한**: 없음
---

## 👤 7. 사용자 프로필 (User Profile)

### 7.1. `U-504`: 내 정보 조회
-   **API**: `GET /api/user/me`
-   **설명**: 현재 로그인한 사용자의 정보를 조회합니다.
-   **권한**: `USER`, `SELLER`, `ADMIN`
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `UserInfoResponse`
        ```json
        {
          "email": "user@example.com",
          "username": "SOMA_USER",
          "role": "USER"
        }
        ```
-   **Axios 예시**
    ```javascript
    const getMyInfo = async () => {
      try {
        const response = await axios.get('/api/user/me', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('내 정보 조회 실패:', error.response.data);
      }
    };
    ```

### 7.2. `U-504`: 내 정보 수정
-   **API**: `PUT /api/user/me`
-   **설명**: 현재 로그인한 사용자의 닉네임, 비밀번호 등을 수정합니다.
-   **권한**: `USER`, `SELLER`, `ADMIN`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "username": "NEW_SOMA_USER",
          "currentPassword": "password123",
          "newPassword": "newPassword456"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    const updateMyInfo = async (updateData) => {
      try {
        await axios.put('/api/user/me', updateData, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('정보 수정 성공!');
      } catch (error) {
        console.error('정보 수정 실패:', error.response.data);
      }
    };
    ```

### 7.3. `U-505`: 회원 탈퇴
-   **API**: `DELETE /api/user/me`
-   **설명**: 현재 로그인한 사용자의 계정을 비활성화(Soft Delete)합니다.
-   **권한**: `USER`, `SELLER`
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    const deactivateMyAccount = async () => {
      try {
        await axios.delete('/api/user/me', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        // 성공 시 로컬 스토리지의 토큰을 삭제하고 로그아웃 처리
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        console.log('회원 탈퇴 성공!');
      } catch (error) {
        console.error('회원 탈퇴 실패:', error.response.data);
      }
    };
    ```

### 2.2. `P-201.1`: 검색 자동완성

-   **API**: `GET /api/products/autocomplete`
-   **설명**: 검색창에 입력 중인 키워드에 대한 자동완성 추천 목록을 제공합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `keyword` (string, required): 2글자 이상의 검색어
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<AutocompleteResponse>`
        ```json
        [
          { "name": "Intel Core i5-13600K" },
          { "name": "Intel Core i5-12400F" }
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 자동완성 제안 요청
    const fetchAutocomplete = async (keyword) => {
      if (keyword.length < 2) return [];
      try {
        const response = await axios.get('/api/products/autocomplete', { params: { keyword } });
        return response.data; // [{name: '...'}, ...]
      } catch (error) {
        console.error('자동완성 조회 실패:', error.response.data);
        return [];
      }
    };
    ```

### 2.3. `P-202`: 상품 상세 조회

-   **API**: `GET /api/products/{productId}`
-   **설명**: 특정 상품의 상세 정보, 기술 사양, 가격 비교 목록 등을 조회합니다.
-   **권한**: 없음
-   **RData (Request Data)**
    -   `Path Parameter`: `productId` (long)
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `ProductDetailResponse` (매우 복잡한 객체, 주요 필드만 예시)
        ```json
        {
          "product": {
            "productId": 101,
            "productName": "[A판매자] Intel Core i5-13600K",
            "price": 350000.00,
            "imageUrl": "/images/some-image.jpg",
            "stock": 50
          },
          "baseSpec": {
            "name": "Intel Core i5-13600K",
            "manufacturer": "Intel",
            "category": "CPU",
            "cpuSpec": {
              "socket": "LGA1700",
              "supportedMemoryTypes": ["DDR5", "DDR4"],
              "hasIgpu": true
            }
          },
          "sellerInfo": { ... },
          "priceComparison": [ ... ]
        }
        ```
-   **Axios 예시**
    ```javascript
    // 상품 상세 정보 조회
    const getProductDetail = async (productId) => {
      try {
        const response = await axios.get(`/api/products/${productId}`);
        console.log('상품 상세 정보:', response.data);
        return response.data;
      } catch (error) {
        console.error('상품 상세 조회 실패:', error.response.data);
      }
    };
    ```

---

## 🛒 3. 장바구니 (Cart)

### 3.1. `P-301`: 장바구니에 상품 추가

-   **API**: `POST /api/cart/items`
-   **설명**: 특정 상품을 지정된 수량만큼 장바구니에 추가합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "productId": 101,
          "quantity": 1
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 장바구니에 상품 추가 (반드시 인증 토큰 필요)
    const addToCart = async (productId, quantity) => {
      try {
        await axios.post('/api/cart/items', { productId, quantity }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('장바구니 추가 성공!');
      } catch (error) {
        console.error('장바구니 추가 실패:', error.response.data);
      }
    };
    ```

### 3.2. `P-301`: 내 장바구니 조회

-   **API**: `GET /api/cart`
-   **설명**: 현재 로그인한 사용자의 장바구니 목록과 전체 견적의 호환성 검사 결과를 조회합니다.
-   **권한**: `USER`
-   **RData (Request Data)**: 없음
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `CartResponse`
        ```json
        {
          "cartItems": [
            {
              "cartItemId": 1,
              "product": { "productId": 101, "productName": "...", ... },
              "quantity": 1
            }
          ],
          "totalPrice": 350000.00,
          "compatibilityResult": {
            "status": "SUCCESS", // SUCCESS, WARN, FAIL
            "messages": []
          }
        }
        ```
-   **Axios 예시**
    ```javascript
    // 내 장바구니 조회
    const getMyCart = async () => {
      try {
        const response = await axios.get('/api/cart', {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('장바구니 정보:', response.data);
        return response.data;
      } catch (error) {
        console.error('장바구니 조회 실패:', error.response.data);
      }
    };
    ```

### 3.3. `P-301`: 장바구니 상품 수량 수정

-   **API**: `PUT /api/cart/items/{cartItemId}`
-   **설명**: 장바구니에 담긴 특정 아이템의 수량을 변경합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Path Parameter`: `cartItemId` (long)
    -   `Body (application/json)`
        ```json
        { "quantity": 2 }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 장바구니 상품 수량 변경
    const updateCartItemQuantity = async (cartItemId, newQuantity) => {
      try {
        await axios.put(`/api/cart/items/${cartItemId}`, { quantity: newQuantity }, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('수량 변경 성공!');
      } catch (error) {
        console.error('수량 변경 실패:', error.response.data);
      }
    };
    ```

### 3.4. `U-301.5`: 장바구니 상품 선택 삭제

-   **API**: `DELETE /api/cart/items`
-   **설명**: 장바구니에서 여러 상품을 한 번에 삭제합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "cartItemIds": [1, 2, 3]
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `204 No Content`
-   **Axios 예시**
    ```javascript
    // 장바구니 선택 삭제
    const deleteCartItems = async (cartItemIds) => {
      try {
        await axios.delete('/api/cart/items', {
          headers: { 'Authorization': localStorage.getItem('accessToken') },
          data: { cartItemIds } // DELETE 요청 시 body는 data 속성에 담아야 함
        });
        console.log('선택 삭제 성공!');
      } catch (error) {
        console.error('선택 삭제 실패:', error.response.data);
      }
    };
    ```

---

## 🧾 4. 주문 (Order)

### 4.1. `P-501`: 주문 생성 (장바구니 기반)

-   **API**: `POST /api/orders`
-   **설명**: 장바구니에 담긴 모든 상품으로 주문을 생성합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "recipientName": "홍길동",
          "shippingAddress": "서울시 강남구 테헤란로",
          "shippingPostcode": "06123"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 생성된 주문의 `orderId` (long)
-   **Axios 예시**
    ```javascript
    // 장바구니 상품으로 주문하기
    const createOrderFromCart = async (shippingInfo) => {
      try {
        const response = await axios.post('/api/orders', shippingInfo, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('주문 성공! Order ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('주문 실패:', error.response.data);
      }
    };
    ```

### 4.2. `P-202`: 즉시 구매

-   **API**: `POST /api/orders/instant`
-   **설명**: 단일 상품을 즉시 구매하는 주문을 생성합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "productId": 101,
          "quantity": 1,
          "recipientName": "홍길동",
          "shippingAddress": "서울시 강남구 테헤란로",
          "shippingPostcode": "06123"
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: 생성된 주문의 `orderId` (long)
-   **Axios 예시**
    ```javascript
    // 즉시 구매하기
    const createInstantOrder = async (orderInfo) => {
      try {
        const response = await axios.post('/api/orders/instant', orderInfo, {
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('즉시 구매 성공! Order ID:', response.data);
        return response.data;
      } catch (error) {
        console.error('즉시 구매 실패:', error.response.data);
      }
    };
    ```

### 4.3. `P-401`: 주문 내역 조회

-   **API**: `GET /api/orders`
-   **설명**: 로그인한 사용자의 주문 내역 목록을 페이징하여 조회합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Query Parameters`: `page`, `size`, `sort` 등 `Pageable` 파라미터
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `Page<OrderListResponseDto>`
-   **Axios 예시**
    ```javascript
    // 내 주문 내역 조회
    const getMyOrders = async (page = 0, size = 10) => {
      try {
        const response = await axios.get('/api/orders', {
          params: { page, size, sort: 'orderedAt,desc' },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        return response.data;
      } catch (error) {
        console.error('주문 내역 조회 실패:', error.response.data);
      }
    };
    ```

---

## 🧠 5. 추천 및 로깅 (Recommendation & Logging)

### 5.1. `U-401`: 개인화 추천 요청

-   **API**: `GET /api/recommendations/personal`
-   **설명**: 사용자의 행동 로그와 장바구니 상태를 기반으로 개인화된 상품을 추천받습니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Query Parameters`:
        -   `eventType` (string, optional, default: `detail-page-view`): 추천을 요청하는 페이지의 컨텍스트
        -   `count` (int, optional, default: `5`): 받고자 하는 추천 상품 개수
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`: `List<RecommendationResponseDto>`
        ```json
        [
          {
            "product": { ... }, // ProductSimpleResponse
            "compatibilityStatus": "SUCCESS",
            "compatibilityMessages": []
          }
        ]
        ```
-   **Axios 예시**
    ```javascript
    // 개인화 추천 상품 요청
    const getPersonalRecommendations = async (count = 5) => {
      try {
        const response = await axios.get('/api/recommendations/personal', {
          params: { count },
          headers: { 'Authorization': localStorage.getItem('accessToken') }
        });
        console.log('추천 상품:', response.data);
        return response.data;
      } catch (error) {
        console.error('추천 요청 실패:', error.response.data);
      }
    };
    ```

### 5.2. `SYS-3`: 프론트엔드 행동 로깅

-   **API**: `POST /api/logs/action`
-   **설명**: 사용자의 주요 행동(상세 페이지 오래 보기, 이미지 클릭 등)을 서버에 기록하여 추천 엔진의 의도 점수를 갱신합니다.
-   **권한**: `USER`
-   **RData (Request Data)**
    -   `Body (application/json)`
        ```json
        {
          "baseSpecId": "base_cpu_intel_intel-core-i9-12900k_bc573357",
          "actionType": "LONG_VIEW" // VIEW, LONG_VIEW, IMAGE_VIEW 등
        }
        ```
-   **SData (Success Data)**
    -   `Status`: `200 OK`
-   **Axios 예시**
    ```javascript
    // 15초 이상 머무를 때 'LONG_VIEW' 로그 전송
    const logLongView = (baseSpecId) => {
      axios.post('/api/logs/action', { baseSpecId, actionType: 'LONG_VIEW' }, {
        headers: { 'Authorization': localStorage.getItem('accessToken') }
      }).catch(err => console.error('로그 전송 실패', err));
    };
    ```

---

## 📁 6. 파일 (File)

### 6.1. `P-601`: 파일 업로드

-   **API**: `POST /api/files/upload`
-   **설명**: 상품/모델 등록/수정 시 이미지를 먼저 서버에 업로드하고, 반환된 파일명을 `imageUrl` 필드에 담아 전송합니다.
-   **권한**: `SELLER`, `ADMIN`
-   **RData (Request Data)**
    -   `Body (multipart/form-data)`: `file` 키로 이미지 파일 전송
-   **SData (Success Data)**
    -   `Status`: `200 OK`
    -   `Body`:
        ```json
        {
          "fileName": "generated_unique_filename.jpg",
          "fileUrl": "http://localhost:8080/images/generated_unique_filename.jpg"
        }
        ```
-   **Axios 예시**
    ```javascript
    // 이미지 파일 업로드
    const uploadImage = async (file) => {
      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await axios.post('/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': localStorage.getItem('accessToken')
          }
        });
        console.log('업로드 성공:', response.data);
        return response.data; // { fileName: '...', fileUrl: '...' }
      } catch (error) {
        console.error('파일 업로드 실패:', error.response.data);
      }
    };
    ```