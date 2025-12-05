package com.kosta.somacom.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;

public interface BaseSpecRepository extends JpaRepository<BaseSpec, String>, BaseSpecRepositoryCustom {

	 List<BaseSpec> findByNameContainingIgnoreCase(String name, Pageable pageable);

	 List<BaseSpec> findTop10ByNameContainingIgnoreCase(String name);

	 Optional<BaseSpec> findFirstByNameIgnoreCase(String name);

	// [신규] 카테고리 이름으로 첫 번째 BaseSpec을 찾는 메소드
	default Optional<BaseSpec> findFirstByCategoryName(String categoryName) {
		try {
			PartCategory category = PartCategory.valueOf(categoryName.toUpperCase());
			return findFirstByCategory(category);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	Optional<BaseSpec> findFirstByCategory(PartCategory category);

	List<BaseSpec> findAllByCategory(PartCategory category);

	// [신규] 카테고리와 이름으로 가장 최신 BaseSpec을 찾는 메소드
	Optional<BaseSpec> findFirstByCategoryAndNameContainingIgnoreCaseOrderByIdDesc(PartCategory category, String name);
}