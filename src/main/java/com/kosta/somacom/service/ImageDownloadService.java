package com.kosta.somacom.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

            // [추가] 이미지 리사이징: 긴 변을 400px로 맞춤 (비율 유지)
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage originalImage = ImageIO.read(bis);
            if (originalImage == null) {
                log.warn("Failed to decode image from: {}", imageUrl);
                return false;
            }

            // [수정] 이미지 리사이징: 높이 400px 기준, 가로 최대 600px 제한 (비율 유지)
            int targetHeight = 400;
            int maxWidth = 600;
            
            int newWidth = (int) (originalImage.getWidth() * ((double) targetHeight / originalImage.getHeight()));
            int newHeight = targetHeight;

            if (newWidth > maxWidth) {
                newWidth = maxWidth;
                newHeight = (int) (originalImage.getHeight() * ((double) maxWidth / originalImage.getWidth()));
            }

            BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = resizedImage.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setColor(java.awt.Color.WHITE);
            g.fillRect(0, 0, newWidth, newHeight);
            g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
            g.dispose();

            Path uploadPath = Paths.get(storagePath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            ImageIO.write(resizedImage, "jpg", filePath.toFile());
            
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
