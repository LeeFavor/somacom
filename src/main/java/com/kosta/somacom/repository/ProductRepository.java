package com.kosta.somacom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.somacom.domain.product.Product;

public interface ProductRepository extends JpaRepository<Product, Long>, ProductRepositoryCustom {
}