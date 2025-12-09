package com.kosta.somacom.config; // 적절한 패키지 경로

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    // 로그를 찍지 않을 URL 패턴 목록
    private static final List<String> EXCLUDE_URL_PATTERNS = Arrays.asList(
            "/api/admin/logs" // 이 URL에 대한 요청은 로그를 찍지 않습니다.
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 제외 패턴에 해당하는지 확인
        String requestUri = request.getRequestURI();
        boolean shouldExclude = EXCLUDE_URL_PATTERNS.stream().anyMatch(requestUri::startsWith);

        if (shouldExclude) {
            // 제외 패턴이면 필터 체인만 계속 진행하고 로그는 찍지 않습니다.
            filterChain.doFilter(request, response);
            return;
        }

        // 그 외 요청은 기존처럼 로그를 찍습니다.
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        long endTime = System.currentTimeMillis();

        logRequest(requestWrapper, responseWrapper, endTime - startTime);

        responseWrapper.copyBodyToResponse(); // 응답 본문을 클라이언트에 다시 복사
    }

    private void logRequest(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, long duration) throws UnsupportedEncodingException {
        String requestBody = new String(request.getContentAsByteArray(), request.getCharacterEncoding());
        String responseBody = new String(response.getContentAsByteArray(), response.getCharacterEncoding());

        log.info("Request: {} {} ({}ms) Headers: {} Body: {}",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                getRequestHeaders(request),
                requestBody.isEmpty() ? "N/A" : requestBody);

        log.info("Response: {} {} ({}ms) Status: {} Body: {}",
                request.getMethod(),
                request.getRequestURI(),
                duration,
                response.getStatus(),
                responseBody.isEmpty() ? "N/A" : responseBody);
    }

    private String getRequestHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(headerName -> headerName + ": " + Collections.list(request.getHeaders(headerName)))
                .collect(Collectors.joining(", "));
    }
}
