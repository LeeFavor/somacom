package com.kosta.somacom.config;

// [추가] Google API의 핵심 인증 공급자
import com.google.api.gax.core.CredentialsProvider; 
import com.google.cloud.retail.v2.PredictionServiceClient;
import com.google.cloud.retail.v2.PredictionServiceSettings;
import com.google.cloud.retail.v2.ProductServiceClient;
import com.google.cloud.retail.v2.ProductServiceSettings;
import com.google.cloud.retail.v2.UserEventServiceClient;
import com.google.cloud.retail.v2.UserEventServiceSettings;
// [추가] @Autowired 사용
import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;


/**
 * Google Vertex AI Search for retail 클라이언트 Bean을 생성합니다.
 * application.properties에 설정된 인증 정보를 자동으로 사용합니다.
 */
@Configuration
public class GoogleAiConfig {

    @Value("${gcp.location}")
    private String location; // "global"

    // [추가] application.properties의 gcp.credentials.location을
    // 기반으로 Spring이 자동으로 생성해준 CredentialsProvider를 주입받습니다.
    @Autowired
    private CredentialsProvider credentialsProvider;

    // 엔드포인트 설정 (예: "retail.googleapis.com:443")
    private String getEndpoint() {
        return location + "-retail.googleapis.com:443";
    }

    /**
     * [1] 카탈로그 저장을 위한 ProductServiceClient
     */
    @Bean
    public ProductServiceClient productServiceClient() throws IOException {
        ProductServiceSettings settings = ProductServiceSettings.newBuilder()
                .setEndpoint(getEndpoint())
                // [수정] 주입받은 인증 정보를 명시적으로 설정합니다.
                .setCredentialsProvider(credentialsProvider)
                .build();
        return ProductServiceClient.create(settings);
    }

    /**
     * [2] 사용자 로그 저장을 위한 UserEventServiceClient
     */
    @Bean
    public UserEventServiceClient userEventServiceClient() throws IOException {
        UserEventServiceSettings settings = UserEventServiceSettings.newBuilder()
                .setEndpoint(getEndpoint())
                 // [수정] 주입받은 인증 정보를 명시적으로 설정합니다.
                .setCredentialsProvider(credentialsProvider)
                .build();
        return UserEventServiceClient.create(settings);
    }

    /**
     * [3] 추천 요청을 위한 PredictionServiceClient
     */
    @Bean
    public PredictionServiceClient predictionServiceClient() throws IOException {
        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setEndpoint(getEndpoint())
                 // [수정] 주입받은 인증 정보를 명시적으로 설정합니다.
                .setCredentialsProvider(credentialsProvider)
                .build();
        return PredictionServiceClient.create(settings);
    }
}