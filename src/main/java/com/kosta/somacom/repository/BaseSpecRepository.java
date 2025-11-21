package com.kosta.somacom.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.somacom.domain.part.BaseSpec;

public interface BaseSpecRepository extends JpaRepository<BaseSpec, String> {

	 List<BaseSpec> findByNameContainingIgnoreCase(String name, Pageable pageable);
}