package com.kosta.somacom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일을 선택해주세요.");
        }

        // 원본 파일명에서 확장자 추출
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // UUID를 사용하여 고유한 파일명 생성
        String storedFileName = UUID.randomUUID().toString() + extension;

        Path uploadPath = Paths.get(uploadDir);
        Files.createDirectories(uploadPath); // 디렉토리가 없으면 생성

        Path filePath = uploadPath.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath);

        return storedFileName;
    }
}