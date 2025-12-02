package com.kosta.somacom.config;

import com.kosta.somacom.config.jwt.JwtProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

	@Value("${file.resource-handler}")
	private String resourceHandler;

	@Value("${file.resource-locations}")
	private String resourceLocations;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
				.allowedOriginPatterns("http://localhost:5173") // 허용할 오리진 패턴
				.allowedMethods("*") // 모든 HTTP 메소드 허용
				.allowedHeaders("*") // 모든 헤더 허용
				.allowCredentials(true) // 쿠키/Authorization 정보 허용
				.exposedHeaders(JwtProperties.HEADER_STRING); // 프론트엔드에서 접근 가능하도록 헤더 노출
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(resourceHandler).addResourceLocations(resourceLocations);
	}
}