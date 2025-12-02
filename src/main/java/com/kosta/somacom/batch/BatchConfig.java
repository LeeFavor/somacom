package com.kosta.somacom.batch;

import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.score.CompatibilityScore;
import com.kosta.somacom.engine.RuleEngineService;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.CompatibilityScoreRepository;
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

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class BatchConfig {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final BaseSpecRepository baseSpecRepository;
    private final CompatibilityScoreRepository compatibilityScoreRepository;
    private final RuleEngineService ruleEngineService;

    private static final int BATCH_CHUNK_SIZE = 1000;

    @Bean
    public Job compatibilityBatchJob() {
        return jobBuilderFactory.get("compatibilityBatchJob")
                .incrementer(new RunIdIncrementer())
                .start(compatibilityCalculationStep())
                .build();
    }

    @Bean
    public Step compatibilityCalculationStep() {
        return stepBuilderFactory.get("compatibilityCalculationStep")
                .tasklet(compatibilityTasklet())
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
}