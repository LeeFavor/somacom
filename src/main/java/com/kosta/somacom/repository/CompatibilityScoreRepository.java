package com.kosta.somacom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.kosta.somacom.domain.score.CompatibilityScore;
import com.kosta.somacom.domain.score.CompatibilityScoreId;

public interface CompatibilityScoreRepository extends JpaRepository<CompatibilityScore, CompatibilityScoreId> {

    @Modifying
    @Query("DELETE FROM CompatibilityScore cs WHERE cs.id.specAId = :specId OR cs.id.specBId = :specId")
    void deleteAllBySpecId(@Param("specId") String specId);
}
