package com.kosta.somacom.service;

import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderStatus;
import com.kosta.somacom.order.dto.OrderDetailResponseDto;
import com.kosta.somacom.payment.dto.PaymentConfirmRequest;
import com.kosta.somacom.payment.dto.TossPaymentResponse;
import com.kosta.somacom.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${payment.toss.secret_key}")
    private String tossSecretKey;

    private static final String TOSS_CONFIRM_URL = "https://api.tosspayments.com/v1/payments/confirm";

    public OrderDetailResponseDto confirmTossPayment(PaymentConfirmRequest request) {
        // 1. DB에서 주문 조회 (payment_order_id 기준, PENDING 상태)
        Order order = orderRepository.findByPaymentOrderIdAndStatus(request.getOrderId(), OrderStatus.PENDING)
                .orElseThrow(() -> new IllegalArgumentException("결제 대기 중인 주문을 찾을 수 없습니다."));

        // 2. 금액 위변조 검증 (DB의 소수점 금액을 반올림하여 정수 금액과 비교)
        // DB에 저장된 금액(e.g., 408.54)을 반올림하여(e.g., 409), 프론트에서 받은 금액과 비교합니다.
        BigDecimal dbAmountRounded = order.getTotalPrice().setScale(0, java.math.RoundingMode.HALF_UP);
        BigDecimal requestAmount = request.getAmount();

        if (dbAmountRounded.compareTo(requestAmount) != 0) {
            order.setStatus(OrderStatus.FAILED);
            log.error("주문 금액 불일치. DB 금액(반올림): {}, 요청 금액: {}", dbAmountRounded, requestAmount);
            throw new IllegalStateException("주문 금액이 일치하지 않습니다. 관리자에게 문의하세요.");
        }

        // 3. 토스 페이먼츠 결제 승인 API 호출
        // 프론트에서 받은 값 대신, 서버에서 검증된 값으로 요청 객체를 다시 만드는 것이 안전합니다.
        PaymentConfirmRequest apiRequest = new PaymentConfirmRequest();
        apiRequest.setPaymentKey(request.getPaymentKey());
        apiRequest.setOrderId(order.getPayment_order_id()); // DB에서 조회한 payment_order_id 사용
        apiRequest.setAmount(request.getAmount());      // [수정] 프론트에서 받은 실제 결제 금액 사용
        TossPaymentResponse tossResponse = requestTossPaymentConfirmation(apiRequest);

        // 4. DB 업데이트 (결제 성공 처리)
        order.updateOnPaymentSuccess(tossResponse.getPaymentKey(), tossResponse.getMethod());

        // 5. 재고 차감
        order.getOrderItems().forEach(item -> {
            item.getProduct().removeStock(item.getQuantity());
        });

        return new OrderDetailResponseDto(order);
    }

    private TossPaymentResponse requestTossPaymentConfirmation(PaymentConfirmRequest request) {
        HttpHeaders headers = new HttpHeaders();

        log.info("Using Toss Secret Key for Basic Auth: '{}'", tossSecretKey);

        // Spring의 setBasicAuth(username, password)를 사용하여 자동으로 인코딩 처리
        headers.setBasicAuth(tossSecretKey, "");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<PaymentConfirmRequest> entity = new HttpEntity<>(request, headers);

        try {
            return restTemplate.postForObject(TOSS_CONFIRM_URL, entity, TossPaymentResponse.class);
        } catch (ResourceAccessException e) {
            log.error("Toss Payments API에 연결할 수 없습니다. 네트워크 또는 방화벽 설정을 확인하세요.", e);
            throw new RuntimeException("결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                log.error("Toss Payments API 401 Unauthorized. Check if the secret key is correct and not expired.");
            }
            // 토스 페이먼츠 API가 4xx 에러를 반환한 경우
            log.error("Toss Payments API 4xx Error: {}", e.getResponseBodyAsString());
            throw new RuntimeException("토스 페이먼츠 결제 승인에 실패했습니다. 응답: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) { // 그 외 모든 예외
            log.error("An unexpected error occurred during Toss Payments confirmation", e);
            throw new RuntimeException("토스 페이먼츠 결제 승인에 실패했습니다.", e);
        }
    }
}