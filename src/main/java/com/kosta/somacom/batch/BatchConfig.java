package com.kosta.somacom.batch;

import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.score.CompatibilityScore;
import com.kosta.somacom.domain.score.PopularityScore;
import com.kosta.somacom.domain.score.PopularityScoreId;
import com.kosta.somacom.engine.PopularityEngineService;
import com.kosta.somacom.engine.RuleEngineService;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.CompatibilityScoreRepository;
import com.kosta.somacom.repository.OrderItemRepository;
import com.kosta.somacom.repository.PopularityScoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BaseSpecRepository baseSpecRepository;
    private final CompatibilityScoreRepository compatibilityScoreRepository;
    private final OrderItemRepository orderItemRepository;
    private final PopularityScoreRepository popularityScoreRepository;
    private final RuleEngineService ruleEngineService;
    private final PopularityEngineService popularityEngineService;
    
    


    private static final int BATCH_CHUNK_SIZE = 1000;

    @Bean
    public Job compatibilityBatchJob() {
        return jobBuilderFactory.get("compatibilityBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(compatibilityCalculationStep())
                .build();
    }

    @Bean
    public Job popularityBatchJob() {
        return jobBuilderFactory.get("popularityBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(popularityCalculationStep())
                .build();
    }

    @Bean
    public Step compatibilityCalculationStep() {
        return stepBuilderFactory.get("compatibilityCalculationStep")
                .tasklet(compatibilityTasklet())
                .build();
    }

    @Bean
    public Step popularityCalculationStep() {
        return stepBuilderFactory.get("popularityCalculationStep")
                .tasklet(popularityTasklet())
                .build();
    }
    @Bean
    public Tasklet compatibilityTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> Compatibility Batch Job Started");

            // 1. 모든 BaseSpec 조회
            List<BaseSpec> allParts = baseSpecRepository.findAll();
            log.info(">>>>> Found {} parts to process.", allParts.size());

            List<CompatibilityScore> scoresToSave = new ArrayList<>();
            long totalCombinations = 0;

            // 2. 모든 조합에 대해 호환성 검사
            for (BaseSpec partA : allParts) {
                for (BaseSpec partB : allParts) {
                    if (partA.getId().equals(partB.getId())) continue; // 자기 자신과의 비교는 건너뜀

                    // 3. 규칙 엔진 실행
                    CompatibilityResult result = ruleEngineService.checkCompatibility(partA, partB);

                    // 4. 결과 엔티티 생성
                    CompatibilityScore score = CompatibilityScore.builder()
                            .specAId(partA.getId())
                            .specBId(partB.getId())
                            .status(result.getStatus())
                            .reasonCode(result.getReasonCode())
                            .build();
                    scoresToSave.add(score);
                    totalCombinations++;

                    // 5. 일정 개수마다 DB에 저장 (메모리 관리)
                    if (scoresToSave.size() >= BATCH_CHUNK_SIZE) {
                        compatibilityScoreRepository.saveAll(scoresToSave);
                        scoresToSave.clear();
                    }
                }
            }

            // 남은 결과 저장
            if (!scoresToSave.isEmpty()) {
                compatibilityScoreRepository.saveAll(scoresToSave);
            }

            log.info(">>>>> Compatibility Batch Job Finished. Total combinations processed: {}", totalCombinations);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet popularityTasklet() {
        return (contribution, chunkContext) -> {
            log.info(">>>>> Popularity Batch Job Started");
            log.info("==================== [DEBUG START] ====================");

            // [수정] 1. 마지막 계산 시점 조회
            LocalDateTime lastCalculatedTime = popularityScoreRepository.findLastCalculatedTime();
            List<OrderItem> newOrderItems;

            log.info("[1] 마지막 계산 시점 조회 결과: {}", lastCalculatedTime);

            if (lastCalculatedTime != null) {
                // [수정] 2. 마지막 계산 시점 이후의 신규 주문만 조회
                log.info(">>>>> Last calculation was at: {}. Fetching new orders since then.", lastCalculatedTime);
                newOrderItems = orderItemRepository.findByOrder_OrderedAtAfter(lastCalculatedTime);
            } else {
                // [수정] 최초 실행 시 모든 주문 조회
                log.info(">>>>> First run. Fetching all orders.");
                newOrderItems = orderItemRepository.findAll();
            }

            log.info("[2] 처리 대상 신규 주문 아이템 개수: {}", newOrderItems.size());
            if (newOrderItems.isEmpty()) {
                log.warn(">>>>> 처리할 신규 주문이 없어 작업을 조기 종료합니다.");
                log.info("==================== [DEBUG END] ======================");
                return RepeatStatus.FINISHED;
            }

            // 2. PopularityEngineService를 사용하여 함께 구매된 상품 조합 및 빈도수 계산
            Map<PopularityScoreId, Long> frequencyMap = popularityEngineService.calculateScores(newOrderItems);
            log.info("[3] 계산된 상품 조합(쌍)의 개수: {}", frequencyMap.size());
            if (log.isDebugEnabled() || frequencyMap.size() < 10) { // 로그가 너무 많아지는 것을 방지
                frequencyMap.forEach((id, score) -> log.info("    - Pair: ({}, {}), Score: {}", id.getSpecAId(), id.getSpecBId(), score));
            }

            // 3. DB에 점수 업데이트 (Upsert)
            // [수정] 새로 구현한 네이티브 쿼리 배치 업데이트 로직을 사용합니다.
            popularityScoreRepository.upsertAll(frequencyMap);

            log.info(">>>>> Popularity Batch Job Finished. Upserted {} score pairs.", frequencyMap.size());
            return RepeatStatus.FINISHED;
        };
    }
}