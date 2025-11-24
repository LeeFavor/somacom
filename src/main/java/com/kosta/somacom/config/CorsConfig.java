package com.kosta.somacom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.kosta.somacom.config.jwt.JwtProperties;

@Configuration
public class CorsConfig {
	@Bean
	public CorsFilter corsFilter() {
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true);  // 쿠키/Authorization 허용
	    config.addAllowedOriginPattern("http://localhost:5173"); // React dev 서버
	    config.addAllowedHeader("*"); // 모든 헤더 허용
	    config.addAllowedMethod("*"); // 모든 메서드 허용
	    config.addExposedHeader(JwtProperties.HEADER_STRING); //react에서 토큰을 헤더로 받기 위해 받드시 필요
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	
}