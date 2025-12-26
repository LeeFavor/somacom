package com.kosta.somacom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kosta.somacom.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    // 상품 이름에 특정 키워드가 포함된 상품 목록을 대소문자 구분 없이 검색
    List<Product> findByNameContainingIgnoreCaseAndIsVisibleTrue(String name);
    
    // S-202: 판매자 상품 목록 조회 (is_visible = true 조건 추가)
    Page<Product> findBySellerIdAndIsVisibleTrue(Long sellerId, Pageable pageable);
    
    // 판매자 ID로 모든 상품 조회 (상태 변경 시 사용)
    List<Product> findBySellerId(Long sellerId);

    // 동일한 기반 모델을 가진 상품 목록 조회 (가격 비교용)
    // List<Product> findByBaseSpec_Id(String baseSpecId); // 이 메소드 대신 아래 페치 조인 사용

    // 배치 작업용: BaseSpec ID로 상품 목록 조회 (Fetch Join 없음)
    List<Product> findByBaseSpecId(String baseSpecId);

    // 페치 조인을 사용하여 Product와 연관된 Seller(User), SellerInfo를 함께 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.seller s LEFT JOIN FETCH s.sellerInfo WHERE p.baseSpec.id = :baseSpecId AND p.isVisible = true")
    List<Product> findWithSellerByBaseSpecId(@Param("baseSpecId") String baseSpecId);

    // 여러 BaseSpec ID에 해당하는 모든 Product를 조회 (추천 결과 변환용)
    @Query("SELECT p FROM Product p JOIN FETCH p.seller s LEFT JOIN FETCH s.sellerInfo WHERE p.baseSpec.id IN :baseSpecIds AND p.isVisible = true AND p.stockQuantity > 0")
    List<Product> findProductsByBaseSpecIds(@Param("baseSpecIds") List<String> baseSpecIds);

    // 상세 조회를 위한 페치 조인: Product, BaseSpec, Seller, SellerInfo를 모두 함께 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.baseSpec WHERE p.id = :productId AND p.isVisible = true")
    Optional<Product> findDetailById(@Param("productId") Long productId);
    
    @Query("SELECT p FROM Product p " +
            "JOIN FETCH p.baseSpec bs " +
            "LEFT JOIN FETCH bs.cpuSpec " +
            "LEFT JOIN FETCH bs.motherboardSpec " +
            "LEFT JOIN FETCH bs.ramSpec " +
            "LEFT JOIN FETCH bs.gpuSpec " +
            "WHERE p.id = :productId AND p.isVisible = true")
     Optional<Product> findWithAllDetailsById(@Param("productId") Long productId);

}