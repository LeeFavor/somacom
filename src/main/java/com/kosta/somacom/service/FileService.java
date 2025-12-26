package com.kosta.somacom.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택해주세요.");
        }

        // [수정] 이미지 리사이징: 높이 400px 고정 (비율 유지)
        BufferedImage originalImage = ImageIO.read(file.getInputStream());
        if (originalImage == null) {
            throw new IllegalArgumentException("이미지 파일이 아니거나 지원하지 않는 포맷입니다.");
        }

        int newHeight = 400;
        int newWidth = (int) (originalImage.getWidth() * ((double) newHeight / originalImage.getHeight()));

        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setColor(java.awt.Color.WHITE); // 투명 배경 대응 (PNG -> JPG 변환 시 검은 배경 방지)
        g.fillRect(0, 0, newWidth, newHeight);
        g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
        g.dispose();

        // 원본 파일명에서 확장자 추출
        String originalFileName = file.getOriginalFilename();
        String extension = "jpg";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf(".") + 1);
        }

        // UUID를 사용하여 고유한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + "." + extension;

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath); // 디렉토리가 없으면 생성

        Path filePath = uploadPath.resolve(storedFileName);
        ImageIO.write(resizedImage, extension, filePath.toFile());

        return storedFileName;
    }
    
    public Resource loadFile(String fileName) throws FileNotFoundException {
        try {
            Path filePath = Paths.get(this.uploadDir).resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }
}