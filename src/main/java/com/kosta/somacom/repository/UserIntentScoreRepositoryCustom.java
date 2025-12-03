package com.kosta.somacom.repository;

import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.domain.score.UserIntentScoreId;

public interface UserIntentScoreRepositoryCustom {
    void upsertScore(UserIntentScoreId id, UserActionType actionType);
}