package com.kosta.somacom.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class LogService {

    // application.properties에 설정된 로그 파일 경로를 주입받습니다.
    @Value("${logging.file.name}")
    private String logFilePath;

    private static final int NUMBER_OF_LINES_TO_READ = 200; // 읽어올 로그 라인 수

    /**
     * 로그 파일의 마지막 N줄을 읽어 문자열로 반환합니다.
     * @return 최신 로그 내용
     */
    public String getLatestLogs() {
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            return "로그 파일을 찾을 수 없습니다: " + logFilePath;
        }

        List<String> lines = new ArrayList<>();
        // RandomAccessFile을 사용하여 파일 끝에서부터 읽습니다.
        try (RandomAccessFile file = new RandomAccessFile(logFile, "r")) {
            long fileLength = file.length() - 1;
            StringBuilder sb = new StringBuilder();
            int lineCount = 0;

            for (long pointer = fileLength; pointer >= 0; pointer--) {
                file.seek(pointer);
                char c = (char) file.read();

                if (c == '\n') {
                    lines.add(sb.reverse().toString());
                    sb.setLength(0); // StringBuilder 초기화
                    lineCount++;
                    if (lineCount >= NUMBER_OF_LINES_TO_READ) {
                        break;
                    }
                } else {
                    sb.append(c);
                }
            }
            // 파일의 첫 줄 처리
            if (sb.length() > 0) {
                lines.add(sb.reverse().toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "로그 파일을 읽는 중 오류가 발생했습니다: " + e.getMessage();
        }

        // 읽은 순서가 역순이므로 다시 뒤집어 시간 순으로 정렬
        Collections.reverse(lines);
        return String.join("\n", lines);
    }
}
