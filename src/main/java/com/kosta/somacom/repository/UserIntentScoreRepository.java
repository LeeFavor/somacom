package com.kosta.somacom.repository;

import com.kosta.somacom.domain.score.UserIntentScore;
import com.kosta.somacom.domain.score.UserIntentScoreId;
import com.kosta.somacom.repository.UserIntentScoreRepositoryCustom;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIntentScoreRepository extends JpaRepository<UserIntentScore, UserIntentScoreId>, UserIntentScoreRepositoryCustom {
    List<UserIntentScore> findById_UserId(String userId);
}