package com.kosta.somacom.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job compatibilityBatchJob; // BatchConfig에 정의된 Job Bean을 주입

    // 매일 새벽 3시에 실행
    @Scheduled(cron = "0 0 3 * * ?")
    public void runCompatibilityJob() {
        log.info("Starting scheduled compatibility batch job...");
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis()) // 매번 다른 파라미터로 Job을 실행하기 위함
                    .toJobParameters();
            jobLauncher.run(compatibilityBatchJob, jobParameters);
        } catch (Exception e) {
            log.error("Error running scheduled compatibility batch job", e);
        }
    }
}