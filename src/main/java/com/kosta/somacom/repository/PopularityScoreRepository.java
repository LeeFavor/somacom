package com.kosta.somacom.repository;

import com.kosta.somacom.domain.score.PopularityScore;
import com.kosta.somacom.domain.score.PopularityScoreId;
import com.kosta.somacom.repository.PopularityScoreRepositoryCustom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PopularityScoreRepository extends JpaRepository<PopularityScore, PopularityScoreId>, PopularityScoreRepositoryCustom {
    @Modifying
    @Query("DELETE FROM PopularityScore ps WHERE ps.id.specAId = :specId OR ps.id.specBId = :specId")
    void deleteAllBySpecId(@Param("specId") String specId);
}