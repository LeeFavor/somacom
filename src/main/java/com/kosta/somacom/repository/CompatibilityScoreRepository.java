package com.kosta.somacom.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kosta.somacom.domain.score.CompatibilityScore;
import com.kosta.somacom.domain.score.CompatibilityScoreId;

public interface CompatibilityScoreRepository extends JpaRepository<CompatibilityScore, CompatibilityScoreId> {

}
