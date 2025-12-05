package com.kosta.somacom.controller;

import com.kosta.somacom.file.dto.FileUploadResponse;
import com.kosta.somacom.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 이미지 파일을 서버에 업로드하고, 저장된 파일명과 접근 URL을 반환합니다.
     * 클라이언트는 이 API를 먼저 호출하여 파일명을 얻은 후,
     * 상품/모델 등록 및 수정 API를 호출할 때 해당 파일명을 imageUrl 필드에 담아 전송해야 합니다.
     * @param file 멀티파트 파일
     * @return 저장된 파일명과 파일 접근 URL
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String fileName = fileService.saveFile(file);

        String fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/images/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(new FileUploadResponse(fileName, fileUrl));
    }

    /**
     * 저장된 이미지 파일을 반환합니다.
     * @param filename 파일명
     * @param request HttpServletRequest
     * @return 이미지 리소스
     * @throws IOException 파일 접근 오류
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, javax.servlet.http.HttpServletRequest request) throws IOException {
        Resource resource = fileService.loadFile(filename);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // MIME 타입을 결정할 수 없는 경우 기본값 설정
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }
}