package com.kosta.somacom.repository;

import com.kosta.somacom.domain.score.PopularityScore;
import com.kosta.somacom.domain.score.PopularityScoreId;
import com.kosta.somacom.repository.PopularityScoreRepositoryCustom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularityScoreRepository extends JpaRepository<PopularityScore, PopularityScoreId>, PopularityScoreRepositoryCustom {
}