package com.kosta.somacom.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.somacom.domain.part.PartCategory;
import com.kosta.somacom.dto.request.BaseSpecCreateRequest;
import com.kosta.somacom.dto.request.CpuSpecDto;
import com.kosta.somacom.dto.request.GpuSpecDto;
import com.kosta.somacom.dto.request.MotherboardSpecDto;
import com.kosta.somacom.dto.request.RamSpecDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataInitializationService {

    private final AdminPartService adminPartService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 애플리케이션이 준비되면 `basespec.txt` 파일로부터 데이터를 읽어 DB를 초기화합니다.
     * DB에 데이터가 이미 있는 경우 중복 생성될 수 있으므로,
     * 초기 1회 실행 후에는 @EventListener를 주석 처리하는 것을 권장합니다.
     */
//    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeData() {
        log.info("Starting database initialization from basespec.txt...");
        try {
            ClassPathResource resource = new ClassPathResource("basespec.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()));

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#")) {
                    continue; // 주석이나 빈 줄은 건너뜁니다.
                }

                String[] parts = line.split("\\|", -1);
                if (parts.length != 5) {
                    log.warn("Skipping malformed line: {}", line);
                    continue;
                }

                try {
                    BaseSpecCreateRequest request = new BaseSpecCreateRequest();
                    request.setCategory(PartCategory.valueOf(parts[0]));
                    request.setManufacturer(parts[1]);
                    request.setName(parts[2]);
                    // imageUrl은 null 처리 (parts[3]가 "null" 문자열일 경우)

                    // JSON 파싱
                    Map<String, Object> specDetails = objectMapper.readValue(parts[4], new TypeReference<>() {});

                    switch (request.getCategory()) {
                        case CPU:
                            request.setCpuSpec(objectMapper.convertValue(specDetails, CpuSpecDto.class));
                            break;
                        case GPU:
                            request.setGpuSpec(objectMapper.convertValue(specDetails, GpuSpecDto.class));
                            break;
                        case Motherboard:
                            request.setMotherboardSpec(objectMapper.convertValue(specDetails, MotherboardSpecDto.class));
                            break;
                        case RAM:
                            request.setRamSpec(objectMapper.convertValue(specDetails, RamSpecDto.class));
                            break;
                    }

                    adminPartService.createBaseSpec(request);
                    count++;

                } catch (Exception e) {
                    log.error("Failed to process line: {}", line, e);
                }
            }
            log.info("Successfully initialized {} BaseSpecs from basespec.txt", count);

        } catch (Exception e) {
            log.error("Failed to initialize database from basespec.txt", e);
        }
    }
}