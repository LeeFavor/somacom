package com.kosta.somacom;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = {"com.kosta.somacom.domain"}) // [수정] 모든 엔티티 패키지를 스캔하도록 설정
@EnableScheduling // [추가] 스케줄링 기능 활성화
public class SomacomApplication {

	public static void main(String[] args) {
		SpringApplication.run(SomacomApplication.class, args);
	}
}
