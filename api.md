# SOMACOM 프론트엔드 API 명세서

이 문서는 SOMACOM 프로젝트의 프론트엔드 개발에 필요한 주요 API의 명세를 정리합니다.

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

## 📦 2. 상품 (Product)

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