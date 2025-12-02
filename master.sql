-- SOMACOM 통합 데이터베이스 스키마
-- (MySQL/MariaDB 10.5+ 기준)
-- 인코딩: UTF8MB4

-- =============================================================
-- 1. 유저, 판매자, 관리자 (USER & AUTH)
-- =============================================================

-- U-101, S-101, A-101: 모든 역할(USER, SELLER, ADMIN)을 포함하는 기본 사용자 테이블
CREATE TABLE users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '시스템 고유 ID',
    email VARCHAR(255) NOT NULL UNIQUE COMMENT '로그인 ID',
    password VARCHAR(255) NOT NULL COMMENT '해시된 비밀번호',
    username VARCHAR(100) NOT NULL UNIQUE COMMENT '닉네임',
    
    `role` VARCHAR(20) NOT NULL COMMENT '사용자 역할 (USER, SELLER_PENDING, SELLER, ADMIN)',
    `status` ENUM('ACTIVE', 'SUSPENDED', 'DEACTIVATED') NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태 (A-102)',
    
    provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' COMMENT '가입 경로 (LOCAL, GOOGLE, NAVER, KAKAO)',
    provider_id VARCHAR(255) UNIQUE COMMENT '소셜 로그인 ID',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '사용자(고객, 판매자, 관리자) 마스터 테이블';

-- S-101: 판매자 비즈니스 정보 (users 테이블과 1:1 관계)
CREATE TABLE seller_info (
    seller_id BIGINT PRIMARY KEY COMMENT 'users.user_id (FK)',
    company_name VARCHAR(255) NOT NULL COMMENT '상호명',
    company_number VARCHAR(100) NOT NULL UNIQUE COMMENT '사업자등록번호',
    phone_number VARCHAR(50) NOT NULL COMMENT '연락처 (S-101)',
    
    FOREIGN KEY (seller_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT '판매자 상세 정보';


-- =============================================================
-- 2. "기반 모델" (Base Specs) - ADMIN 관리
-- =============================================================

-- A-201: Admin이 등록하는 "기반 모델" (예: "Intel i5-13600K")
-- SOMACOM의 모든 호환성 검사(SYS-101)와 상품(products)의 기준이 됨.
CREATE TABLE base_specs (
    base_spec_id VARCHAR(100) PRIMARY KEY COMMENT '기반 모델 고유 ID (예: base_13600k)',
    `name` VARCHAR(255) NOT NULL COMMENT '기반 모델명 (예: Intel Core i5-13600K)',
    manufacturer VARCHAR(100) NOT NULL COMMENT '제조사 (예: Intel)',
    category VARCHAR(20) NOT NULL COMMENT '부품 카테고리 (CPU, GPU, RAM, ...)',
    image_url VARCHAR(255) COMMENT '기반 모델 대표 이미지 URL',
    
    -- (A-201) Admin에 의해 등록/수정됨
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT '기반 모델(사양) 마스터 테이블 (Admin 관리)';

-- A-201: CPU 상세 사양 (html: form_CPU)
CREATE TABLE cpu_specs (
    base_spec_id VARCHAR(100) PRIMARY KEY,
    socket VARCHAR(100) NOT NULL COMMENT '소켓 (SYS-101)',
    supported_memory_types VARCHAR(255) COMMENT '지원 메모리 (예: DDR5,DDR4)',
    has_igpu BOOLEAN DEFAULT FALSE COMMENT '내장그래픽 유무',
    
    FOREIGN KEY (base_spec_id) REFERENCES base_specs(base_spec_id) ON DELETE CASCADE
) COMMENT 'CPU 상세 사양';

-- A-201: Motherboard 상세 사양 (html: form_Motherboard)
CREATE TABLE motherboard_specs (
    base_spec_id VARCHAR(100) PRIMARY KEY,
    socket VARCHAR(100) NOT NULL COMMENT '소켓 (SYS-101)',
    chipset VARCHAR(100) NOT NULL COMMENT '칩셋 (SYS-101)',
    memory_type VARCHAR(20) NOT NULL COMMENT '메모리 타입 (SYS-101)',
    memory_slots INT NOT NULL COMMENT '메모리 슬롯 수',
    form_factor VARCHAR(50) COMMENT '폼팩터 (ATX, mATX)',
    -- GPU 호환성 검사를 위한 주 PCIe 슬롯 정보
    pcie_version DECIMAL(3,1) COMMENT '주요 PCIe 슬롯 버전 (GPU용)',
    pcie_lanes INT COMMENT '주요 PCIe 슬롯 레인 (GPU용)',
    
    FOREIGN KEY (base_spec_id) REFERENCES base_specs(base_spec_id) ON DELETE CASCADE
) COMMENT '메인보드 상세 사양';

-- A-201: RAM 상세 사양 (html: form_RAM)
CREATE TABLE ram_specs (
    base_spec_id VARCHAR(100) PRIMARY KEY,
    memory_type VARCHAR(20) NOT NULL COMMENT '메모리 타입 (SYS-101)',
    speed_mhz INT NOT NULL COMMENT '동작 속도',
    capacity_gb INT NOT NULL COMMENT '모듈 1개당 용량',
    kit_quantity INT NOT NULL DEFAULT 1 COMMENT '킷 수량 (1개 or 2개 세트)',
    height_mm INT COMMENT '모듈 높이 (SYS-101 쿨러 간섭)',
    
    FOREIGN KEY (base_spec_id) REFERENCES base_specs(base_spec_id) ON DELETE CASCADE
) COMMENT 'RAM 상세 사양';

-- A-201: GPU 상세 사양 (html: form_GPU)
CREATE TABLE gpu_specs (
    base_spec_id VARCHAR(100) PRIMARY KEY,
    pcie_version DECIMAL(3,1) NOT NULL COMMENT 'PCIe 버전 (예: 4.0)',
    pcie_lanes INT NOT NULL COMMENT 'PCIe 레인 (예: 16)',
    length_mm INT COMMENT '카드 길이 (SYS-101 케이스 간섭)',
    
    FOREIGN KEY (base_spec_id) REFERENCES base_specs(base_spec_id) ON DELETE CASCADE
) COMMENT 'GPU 상세 사양';

-- (기타 Storage, Cooler, Case, Power 스펙 테이블 생략)


-- =============================================================
-- 3. "판매 상품" (Products) - SELLER 관리
-- =============================================================

-- S-201.3: Seller가 등록하는 "판매 상품" (예: "[ASUS] RTX 4070 (리퍼)")
CREATE TABLE products (
    product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    base_spec_id VARCHAR(100) NOT NULL COMMENT '연결된 기반 모델 ID (FK)',
    seller_id BIGINT NOT NULL COMMENT '판매자 ID (FK)',
    
    `name` VARCHAR(255) NOT NULL COMMENT '판매자 상품명 (예: [ASUS] TUF RTX 4070 OC)',
    price DECIMAL(10, 2) NOT NULL COMMENT '판매가',
    stock_quantity INT NOT NULL DEFAULT 0 COMMENT '재고',
    `condition` VARCHAR(20) NOT NULL DEFAULT 'New' COMMENT '상품 상태 (New, Refurbished)',
    shipping_fee DECIMAL(10, 2) NOT NULL DEFAULT 0 COMMENT '배송비',
    description TEXT COMMENT '판매자 상세 설명',
    
    image_url VARCHAR(255) COMMENT '상품 대표 이미지 URL',
    -- (A-102) Admin이 판매자를 비활성하면 상품도 비노출
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (base_spec_id) REFERENCES base_specs(base_spec_id),
    FOREIGN KEY (seller_id) REFERENCES users(user_id)
) COMMENT '판매자 등록 상품 (실제 판매 단위)';

-- S-201.2, A-203: 판매자의 기반 모델 등록 요청
CREATE TABLE base_spec_requests (
    request_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id BIGINT NOT NULL,
    requested_model_name VARCHAR(255) NOT NULL COMMENT '요청 모델명 (예: Nvidia RTX 5080)',
    category VARCHAR(20) NOT NULL COMMENT '요청 부품 카테고리',
    manufacturer VARCHAR(100) NOT NULL COMMENT '요청 제조사',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '상태 (PENDING, APPROVED, REJECTED)',
    admin_notes TEXT COMMENT 'Admin의 거절 사유 등',
    
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    
    FOREIGN KEY (seller_id) REFERENCES users(user_id)
) COMMENT '판매자의 기반 모델 등록 요청 큐';


-- =============================================================
-- 4. 주문, 장바구니, 리뷰 (USER Interaction)
-- =============================================================

-- U-301: 가상 견적 (장바구니) 마스터
CREATE TABLE carts (
    cart_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE COMMENT '사용자 ID (FK)',
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) COMMENT '가상 견적(장바구니) 마스터';

-- U-301: 가상 견적 아이템
CREATE TABLE cart_items (
    cart_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cart_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (cart_id) REFERENCES carts(cart_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
) COMMENT '가상 견적(장바구니) 내역';

-- U-501: 주문 마스터
CREATE TABLE orders (
    order_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL COMMENT '총 결제 금액',
    
    -- 배송지 정보
    recipient_name VARCHAR(100) NOT NULL,
    shipping_address TEXT NOT NULL,
    shipping_postcode VARCHAR(20),
    
    `status` VARCHAR(20) NOT NULL COMMENT '주문 상태 (PENDING, PAID, ...)',
    
    ordered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    -- (결제 정보, PG사 ID 등 추가될 수 있음)
    ,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT '주문 마스터 테이블';

-- U-501: 주문 아이템 (S-301에서 판매자가 조회)
CREATE TABLE order_items (
    order_item_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    price_at_purchase DECIMAL(10, 2) NOT NULL COMMENT '주문 시점 가격 (고정)',
    
    `status` VARCHAR(20) NOT NULL COMMENT '개별 아이템 상태 (PAID, PREPARING, ...)',
    tracking_number VARCHAR(100) COMMENT '송장 번호 (S-302)',
    
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id)
) COMMENT '주문 상세 내역 (판매자 정산 기준)';

/*
-- [주석 처리] U-503: 리뷰 (현재 엔티티 없음)
CREATE TABLE reviews (
    review_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_item_id BIGINT NOT NULL UNIQUE COMMENT '구매 확정된 아이템 1건당 1리뷰',
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id)
) COMMENT '상품 리뷰';
*/

-- =============================================================
-- 5. 시스템 엔진 (SYS-101, 102, 103)
-- =============================================================

-- SYS-101: 호환성 검사 결과 (U-302, U-401.1)
CREATE TABLE product_compatibility_scores (
    spec_a_id VARCHAR(100) NOT NULL COMMENT 'base_spec_id (예: CPU)',
    spec_b_id VARCHAR(100) NOT NULL COMMENT 'base_spec_id (예: Motherboard)',
    `status` VARCHAR(20) NOT NULL COMMENT '호환성 상태 (SUCCESS, WARN, FAIL)',
    reason_code VARCHAR(255) COMMENT '사유 (예: SOCKET_MISMATCH)',
    
    last_checked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (spec_a_id, spec_b_id)
) COMMENT '호환성 규칙 엔진 결과 (배치 작업)';

-- SYS-102: 인기도(함께 구매) 점수 (U-401.1, U-204)
CREATE TABLE product_popularity_scores (
    spec_a_id VARCHAR(100) NOT NULL COMMENT 'base_spec_id',
    spec_b_id VARCHAR(100) NOT NULL COMMENT 'base_spec_id',
    score BIGINT NOT NULL DEFAULT 0 COMMENT '함께 구매된 빈도수',
    
    last_calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (spec_a_id, spec_b_id)
) COMMENT '인기도(함께 구매) 엔진 결과 (배치 작업)';

-- SYS-103: 사용자 의도 점수 (AI 하이브리드 솔루션)
CREATE TABLE user_intent_score (
    user_id VARCHAR(100) NOT NULL,
    category VARCHAR(100) NOT NULL COMMENT 'CPU, GPU, RAM, Motherboard',
    attribute_tag VARCHAR(100) NOT NULL COMMENT '호환 조건 태그 (예: LGA1700, DDR5)',

    -- 행동 횟수 (Log(n)의 'n' 값)
    view_count INT DEFAULT 0,
    long_view_count INT DEFAULT 0,
    image_view_count INT DEFAULT 0,
    search_count INT DEFAULT 0,
    filter_count INT DEFAULT 0,
    wishlist_count INT DEFAULT 0,
    cart_count INT DEFAULT 0,
    purchase_count INT DEFAULT 0,

    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    PRIMARY KEY (user_id, category, attribute_tag)
) COMMENT '사용자 의도 점수 요약 테이블 (AI 필터링용)';


/*
-- [주석 처리] SYS-103.1, U-70x: 원본 사용자 행동 로그 (AI 학습 원본, 현재 사용 안 함)
CREATE TABLE user_activity_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL COMMENT '사용자 ID (비회원일 시 세션 ID)',
    event_type VARCHAR(50) NOT NULL COMMENT 'VIEW, SEARCH, FILTER, CART_ADD 등',
    target_base_spec_id VARCHAR(100) COMMENT '관련된 기반 모델 ID',
    target_product_id BIGINT COMMENT '관련된 판매 상품 ID',
    event_context TEXT COMMENT 'JSON 형태의 추가 정보 (예: {"query": "i7 14700k"}, {"filter": "socket=LGA1700"})',
    
    event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_user_activity_user (user_id, event_timestamp)
) COMMENT '모든 사용자 원본 행동 로그 (AI 전송용)';
*/