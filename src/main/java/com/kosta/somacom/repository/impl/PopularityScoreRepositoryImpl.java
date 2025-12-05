package com.kosta.somacom.repository.impl;

import com.kosta.somacom.domain.score.PopularityScoreId;
import com.kosta.somacom.repository.PopularityScoreRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;

import java.sql.Types;
import java.sql.PreparedStatement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

        String sql = "INSERT INTO product_popularity_scores (specaid, specbid, score, last_calculated_at) VALUES (?, ?, ?, NOW()) " +
                     "ON DUPLICATE KEY UPDATE score = score + VALUES(score), last_calculated_at = NOW()";

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

    @Override
    public Map<String, Long> findScoresForItems(List<String> targetSpecIds, List<String> contextSpecIds) {
        if (targetSpecIds == null || targetSpecIds.isEmpty() || contextSpecIds == null || contextSpecIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // SQL IN 절을 만들기 위한 파라미터 문자열 생성
        String targetPlaceholders = String.join(",", Collections.nCopies(targetSpecIds.size(), "?"));
        String contextPlaceholders = String.join(",", Collections.nCopies(contextSpecIds.size(), "?"));

        String sql = "SELECT specaid, specbid, score FROM product_popularity_scores " +
                     "WHERE (specaid IN (" + targetPlaceholders + ") AND specbid IN (" + contextPlaceholders + ")) " +
                     "OR (specaid IN (" + contextPlaceholders + ") AND specbid IN (" + targetPlaceholders + "))";

        List<Object> params = new ArrayList<>();
        params.addAll(targetSpecIds);
        params.addAll(contextSpecIds);
        params.addAll(contextSpecIds);
        params.addAll(targetSpecIds);

        Map<String, Long> resultMap = new HashMap<>();

        jdbcTemplate.query(sql, ps -> {
            int i = 1;
            for (Object param : params) {
                ps.setObject(i++, param);
            }
        }, rs -> {
            String specA = rs.getString("specaid");
            String specB = rs.getString("specbid");
            long score = rs.getLong("score");

            resultMap.merge(targetSpecIds.contains(specA) ? specA : specB, score, Long::sum);
        });

        return resultMap;
    }
}