package com.kosta.somacom.service;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.part.CpuSpec;
import com.kosta.somacom.domain.part.GpuSpec;
import com.kosta.somacom.domain.part.MotherboardSpec;
import com.kosta.somacom.domain.part.RamSpec;
import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.domain.score.UserIntentScoreId;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.UserIntentScoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserIntentLoggingService {

    private final BaseSpecRepository baseSpecRepository;
    private final UserIntentScoreRepository userIntentScoreRepository;
    private final PartSpecRepositories partSpecRepositories;

    /**
     * 사용자의 행동을 기반으로 의도 점수를 기록합니다.
     * @param userId 사용자 ID
     * @param baseSpecId 행동이 발생한 기반 모델 ID
     * @param actionType 행동 유형 (VIEW, CART, PURCHASE 등)
     */
    @Transactional
    public void logAction(String userId, String baseSpecId, UserActionType actionType) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(baseSpecId)) {
            return; // 비로그인 사용자 또는 대상이 없으면 무시
        }

        BaseSpec baseSpec = baseSpecRepository.findById(baseSpecId)
                .orElse(null);

        if (baseSpec == null) {
            log.warn("BaseSpec not found for id: {}. Skipping intent logging.", baseSpecId);
            return;
        }

        Set<UserIntentScoreId> intentIds = extractIntentIds(userId, baseSpec);

        for (UserIntentScoreId id : intentIds) {
            userIntentScoreRepository.upsertScore(id, actionType);
        }
        log.info("Logged action '{}' for user '{}' on {} tags.", actionType, userId, intentIds.size());
    }

    /**
     * 특정 태그에 대해 직접 의도 점수를 기록합니다. (검색, 필터링 등)
     * @param userId 사용자 ID
     * @param category 태그의 카테고리 (e.g., "search_keyword", "socket")
     * @param attributeTag 속성 태그 (e.g., "i5-13600k", "LGA1700")
     * @param actionType 행동 유형 (SEARCH, FILTER 등)
     */
    @Transactional
    public void logTagAction(String userId, String category, String attributeTag, UserActionType actionType) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(category) || !StringUtils.hasText(attributeTag)) {
            return;
        }
        UserIntentScoreId id = new UserIntentScoreId(userId, category.toLowerCase(), attributeTag.toLowerCase());
        userIntentScoreRepository.upsertScore(id, actionType);
        log.info("Logged tag action '{}' for user '{}' on tag '{}:{}'.", actionType, userId, category, attributeTag);
    }

    /**
     * BaseSpec에서 의도 태그를 추출하여 UserIntentScoreId 목록을 생성합니다.
     * @param userId 사용자 ID
     * @param baseSpec 기반 모델
     * @return 추출된 의도 ID Set
     */
    private Set<UserIntentScoreId> extractIntentIds(String userId, BaseSpec baseSpec) {
        Set<UserIntentScoreId> ids = new HashSet<>();
        String category = baseSpec.getCategory().toString().toUpperCase();
        String baseSpecId = baseSpec.getId();

        switch (category) {
            case "CPU":
                partSpecRepositories.getCpuSpecRepository().findById(baseSpecId).ifPresent(spec -> {
                    addTag(ids, userId, "socket", spec.getSocket());
                    if (StringUtils.hasText(spec.getSupportedMemoryTypes())) {
                        Arrays.stream(spec.getSupportedMemoryTypes().split(","))
                                .map(String::trim)
                                .forEach(memType -> addTag(ids, userId, "memory", memType));
                    }
                });
                break;
            case "MOTHERBOARD":
                partSpecRepositories.getMotherboardSpecRepository().findById(baseSpecId).ifPresent(spec -> {
                    addTag(ids, userId, "socket", spec.getSocket());
                    addTag(ids, userId, "chipset", spec.getChipset());
                    addTag(ids, userId, "memory", spec.getMemoryType());
                });
                break;
            case "RAM":
                partSpecRepositories.getRamSpecRepository().findById(baseSpecId).ifPresent(spec -> {
                    addTag(ids, userId, "memory", spec.getMemoryType());
                    addTag(ids, userId, "speed", String.valueOf(spec.getSpeedMhz()));
                });
                break;
            case "GPU":
                partSpecRepositories.getGpuSpecRepository().findById(baseSpecId).ifPresent(spec -> {
                    addTag(ids, userId, "pcie", String.valueOf(spec.getPcieVersion()));
                });
                break;
            default:
                log.warn("No intent extraction logic for category: {}", category);
                break;
        }

        // 모든 부품에 공통적으로 '제조사' 태그 추가
        if (StringUtils.hasText(baseSpec.getManufacturer())) {
            addTag(ids, userId, "manufacturer", baseSpec.getManufacturer());
        }

        return ids;
    }

    private void addTag(Set<UserIntentScoreId> ids, String userId, String category, String attribute) {
        if (StringUtils.hasText(userId) && StringUtils.hasText(category) && StringUtils.hasText(attribute)) {
            // 모든 태그를 소문자로 변환하여 일관성 유지
            ids.add(new UserIntentScoreId(userId, category.toLowerCase(), attribute.toLowerCase()));
        }
    }
}