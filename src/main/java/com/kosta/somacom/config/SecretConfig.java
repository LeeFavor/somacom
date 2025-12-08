package com.kosta.somacom.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@PropertySource("classpath:secret.properties")
public class SecretConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        // 연결 타임아웃 5초
        factory.setConnectTimeout(5000);
        // 읽기 타임아웃 5초
        factory.setReadTimeout(5000);

        return new RestTemplate(factory);
    }
}