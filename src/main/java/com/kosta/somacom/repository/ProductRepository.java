package com.kosta.somacom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.somacom.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {

    // 상품 이름에 특정 키워드가 포함된 상품 목록을 대소문자 구분 없이 검색
    List<Product> findByNameContainingIgnoreCase(String name);
}