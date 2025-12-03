package com.kosta.somacom.repository.impl;

import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.domain.score.UserIntentScoreId;
import com.kosta.somacom.repository.UserIntentScoreRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserIntentScoreRepositoryImpl implements UserIntentScoreRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void upsertScore(UserIntentScoreId id, UserActionType actionType) {
        // Enum을 사용하여 SQL 컬럼명을 안전하게 가져옵니다.
        String columnName = actionType.getColumnName();

        // 모든 UserActionType의 컬럼 이름을 가져와서, 현재 actionType을 제외한 나머지에 0을 설정합니다.
        String otherColumns = Arrays.stream(UserActionType.values())
                .map(UserActionType::getColumnName)
                .filter(cn -> !cn.equals(columnName))
                .collect(Collectors.joining(", "));

        String otherColumnsWithZero = Arrays.stream(UserActionType.values())
                .filter(type -> !type.getColumnName().equals(columnName))
                .map(type -> type.getColumnName() + " = 0")
                .collect(Collectors.joining(", "));

        // SQL Injection을 방지하기 위해 컬럼명은 직접 검증된 값을 사용합니다.
        String sql = String.format(
                "INSERT INTO user_intent_score (user_id, category, attribute_tag, %s, %s, last_updated) " +
                "VALUES (?, ?, ?, 1, 0, 0, 0, 0, 0, 0, 0, NOW()) " + // 다른 컬럼들에는 0을 삽입하기 위해 플레이스홀더 추가 (실제 값은 0)
                "ON DUPLICATE KEY UPDATE %s = %s + 1, last_updated = NOW()",
                columnName, otherColumns, columnName, columnName
        );

        jdbcTemplate.update(sql, id.getUserId(), id.getCategory(), id.getAttributeTag());
    }
}