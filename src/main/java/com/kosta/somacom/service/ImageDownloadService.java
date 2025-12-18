package com.kosta.somacom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDownloadService {

    @Value("${app.image.storage-path}")
    private String storagePath;

    private final RestTemplate restTemplate;

    /**
     * URL에서 이미지를 다운로드하여 로컬 스토리지에 저장합니다.
     * @param imageUrl 이미지 URL
     * @param fileName 저장할 파일명 (확장자 포함)
     * @return 저장 성공 여부
     */
    public boolean downloadAndSave(String imageUrl, String fileName) {
        if (!StringUtils.hasText(imageUrl)) {
            return false;
        }

        try {
            byte[] imageBytes = restTemplate.getForObject(imageUrl, byte[].class);
            if (imageBytes == null || imageBytes.length == 0) {
                log.warn("Downloaded empty image from: {}", imageUrl);
                return false;
            }

            Path uploadPath = Paths.get(storagePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.write(filePath, imageBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            
            log.info("Image saved successfully: {}", filePath);
            return true;

        } catch (IOException e) {
            log.error("Failed to save image file: {}", fileName, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to download image from: {}", imageUrl, e);
            return false;
        }
    }
}
