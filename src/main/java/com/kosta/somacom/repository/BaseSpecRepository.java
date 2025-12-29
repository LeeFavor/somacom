package com.kosta.somacom.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.PartCategory;

public interface BaseSpecRepository extends JpaRepository<BaseSpec, String>, BaseSpecRepositoryCustom {

	 List<BaseSpec> findByNameContainingIgnoreCaseAndIsDeletedFalse(String name, Pageable pageable);

	 List<BaseSpec> findTop10ByNameContainingIgnoreCaseAndIsDeletedFalse(String name);

	 Optional<BaseSpec> findFirstByNameIgnoreCaseAndIsDeletedFalse(String name);

	// [신규] 카테고리 이름으로 첫 번째 BaseSpec을 찾는 메소드
	default Optional<BaseSpec> findFirstByCategoryName(String categoryName) {
		try {
			PartCategory category = PartCategory.valueOf(categoryName.toUpperCase());
			return findFirstByCategoryAndIsDeletedFalse(category);
		} catch (IllegalArgumentException e) {
			return Optional.empty();
		}
	}

	Optional<BaseSpec> findFirstByCategoryAndIsDeletedFalse(PartCategory category);

	List<BaseSpec> findAllByCategoryAndIsDeletedFalse(PartCategory category);

	// [신규] 카테고리와 이름으로 가장 최신 BaseSpec을 찾는 메소드
	Optional<BaseSpec> findFirstByCategoryAndNameContainingIgnoreCaseAndIsDeletedFalseOrderByIdDesc(PartCategory category, String name);

	@Query("SELECT DISTINCT cs.socket FROM CpuSpec cs JOIN cs.baseSpec bs WHERE bs.isDeleted = false")
	Set<String> findDistinctCpuSockets();

	@Query(value = "SELECT DISTINCT TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(cs.supported_memory_types, ',', n.n), ',', -1)) FROM cpu_specs cs JOIN base_specs bs ON cs.base_spec_id = bs.base_spec_id JOIN (SELECT 1 AS n UNION ALL SELECT 2 UNION ALL SELECT 3) n ON CHAR_LENGTH(cs.supported_memory_types) - CHAR_LENGTH(REPLACE(cs.supported_memory_types, ',', '')) >= n.n - 1 WHERE bs.is_deleted = false", nativeQuery = true)
	Set<String> findDistinctCpuMemoryTypes();

	@Query("SELECT DISTINCT ms.chipset FROM MotherboardSpec ms JOIN ms.baseSpec bs WHERE bs.isDeleted = false")
	Set<String> findDistinctMotherboardChipsets();

	@Query("SELECT DISTINCT ms.memoryType FROM MotherboardSpec ms JOIN ms.baseSpec bs WHERE bs.isDeleted = false")
	Set<String> findDistinctMotherboardMemoryTypes();

	@Query("SELECT DISTINCT rs.memoryType FROM RamSpec rs JOIN rs.baseSpec bs WHERE bs.isDeleted = false")
	Set<String> findDistinctRamMemoryTypes();

	@Query("SELECT DISTINCT rs.speedMhz FROM RamSpec rs JOIN rs.baseSpec bs WHERE bs.isDeleted = false ORDER BY rs.speedMhz")
	Set<Integer> findDistinctRamSpeeds();

	@Query("SELECT DISTINCT gs.pcieVersion FROM GpuSpec gs JOIN gs.baseSpec bs WHERE bs.isDeleted = false ORDER BY gs.pcieVersion")
	Set<Double> findDistinctGpuPcieVersions();
	
	// Motherboard 관련 필터 옵션 조회
    @Query("SELECT DISTINCT m.socket FROM MotherboardSpec m WHERE m.socket IS NOT NULL ORDER BY m.socket")
    Set<String> findDistinctMotherboardSockets();

    @Query("SELECT DISTINCT m.formFactor FROM MotherboardSpec m WHERE m.formFactor IS NOT NULL ORDER BY m.formFactor")
    Set<String> findDistinctMotherboardFormFactors();

}