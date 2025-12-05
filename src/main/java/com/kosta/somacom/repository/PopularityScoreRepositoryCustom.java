package com.kosta.somacom.repository;

import com.kosta.somacom.domain.score.PopularityScoreId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PopularityScoreRepositoryCustom {
    void upsertAll(Map<PopularityScoreId, Long> scores);
    LocalDateTime findLastCalculatedTime();
    Map<String, Long> findScoresForItems(List<String> targetSpecIds, List<String> contextSpecIds);
}