package com.kosta.somacom.repository.impl;

import com.kosta.somacom.domain.score.PopularityScoreId;
import com.kosta.somacom.repository.PopularityScoreRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class PopularityScoreRepositoryImpl implements PopularityScoreRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void upsertAll(Map<PopularityScoreId, Long> scores) {
        if (scores == null || scores.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO product_popularity_scores (specaid, specbid, score, last_calculated_at) VALUES (?, ?, ?, now()) " +
                     "ON DUPLICATE KEY UPDATE score = score + VALUES(score), last_calculated_at = now()";

        jdbcTemplate.batchUpdate(sql, scores.entrySet(), scores.size(),
                (PreparedStatement ps, Map.Entry<PopularityScoreId, Long> entry) -> {
                    ps.setString(1, entry.getKey().getSpecAId());
                    ps.setString(2, entry.getKey().getSpecBId());
                    ps.setLong(3, entry.getValue());
                });
    }

    @Override
    public LocalDateTime findLastCalculatedTime() {
        String sql = "SELECT MAX(last_calculated_at) FROM product_popularity_scores";
        try {
            // queryForObject가 null을 반환할 수 있으므로, 반환 타입을 명시적으로 지정합니다.
            return jdbcTemplate.queryForObject(sql, LocalDateTime.class);
        } catch (EmptyResultDataAccessException e) {
            return null; // 테이블이 비어있으면 null을 반환합니다.
        }
    }
}