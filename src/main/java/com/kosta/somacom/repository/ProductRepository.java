package com.kosta.somacom.repository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.kosta.somacom.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    // 상품 이름에 특정 키워드가 포함된 상품 목록을 대소문자 구분 없이 검색
    List<Product> findByNameContainingIgnoreCase(String name);

    // 동일한 기반 모델을 가진 상품 목록 조회 (가격 비교용)
    // List<Product> findByBaseSpec_Id(String baseSpecId); // 이 메소드 대신 아래 페치 조인 사용

    // 페치 조인을 사용하여 Product와 연관된 Seller(User), SellerInfo를 함께 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.seller s JOIN FETCH s.sellerInfo WHERE p.baseSpec.id = :baseSpecId")
    List<Product> findWithSellerByBaseSpecId(@Param("baseSpecId") String baseSpecId);

    // 상세 조회를 위한 페치 조인: Product, BaseSpec, Seller, SellerInfo를 모두 함께 조회
    @Query("SELECT p FROM Product p JOIN FETCH p.baseSpec WHERE p.id = :productId")
    Optional<Product> findDetailById(@Param("productId") Long productId);
}