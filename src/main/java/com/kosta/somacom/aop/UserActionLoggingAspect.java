package com.kosta.somacom.aop;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.order.dto.InstantOrderRequest;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.service.RecommendationService;
import com.kosta.somacom.repository.BaseSpecRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.service.UserIntentLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionLoggingAspect {

    private final UserIntentLoggingService userIntentLoggingService;
    private final RecommendationService recommendationService; // Google Cloud 로그 전송을 위해 추가
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;
    private final BaseSpecRepository baseSpecRepository;
    private final CacheManager cacheManager; // 사용자 조회 기록 캐시를 위해 추가

    /**
     * 상품 상세 페이지 조회 시 'VIEW' 액션을 로깅하고, Google Cloud에 최근 조회 기록을 전송합니다. (P-202)
     */
    @AfterReturning(pointcut = "execution(* com.kosta.somacom.controller.ProductDetailController.getProductDetail(..)) && args(productId)", returning = "response")
    public void logProductView(JoinPoint joinPoint, Long productId, ResponseEntity<?> response) {
        getPrincipalDetails().ifPresent(principal -> {
            productRepository.findById(productId).ifPresent(product -> {
                String userId = String.valueOf(principal.getUser().getId());
                String baseSpecId = product.getBaseSpec().getId();

                log.info("Logging 'VIEW' action for user '{}' on baseSpec '{}'", userId, baseSpecId);

                // 1. 로컬 DB에 의도 점수 로깅
                userIntentLoggingService.logAction(
                        userId,
                        baseSpecId,
                        UserActionType.VIEW
                );

                // 2. [신규] 사용자 조회 기록 캐시 및 Google Cloud 로그 전송
                updateAndIngestViewHistory(userId, baseSpecId);
            });
        });
    }

    private void updateAndIngestViewHistory(String userId, String newBaseSpecId) {
        Cache cache = cacheManager.getCache("userViewHistory");
        if (cache == null) {
            log.warn("Cache 'userViewHistory' not found. Skipping Google Cloud event ingestion.");
            return;
        }

        // 캐시에서 사용자의 조회 기록을 가져옴 (LinkedList로 타입 캐스팅)
        LinkedList<String> history = cache.get(userId, LinkedList.class);
        if (history == null) {
            history = new LinkedList<>();
        }

        history.remove(newBaseSpecId); // 중복 제거
        history.addFirst(newBaseSpecId); // 가장 최근에 본 상품을 맨 앞에 추가
        while (history.size() > 5) { history.removeLast(); } // 5개 초과 시 가장 오래된 기록 삭제

        cache.put(userId, history); // 캐시 업데이트

        log.info("Preparing to ingest user view history to Google Cloud. User: {}, History: {}", userId, history);

        // Google Cloud에 최근 조회 기록 5개를 "detail-page-view" 이벤트로 전송
        recommendationService.ingestUserEventsToGoogleCloud(userId, "detail-page-view", history);
    }

    /**
     * 장바구니에 상품 추가 시 'CART' 액션을 로깅합니다. (P-301)
     */
    @AfterReturning("execution(* com.kosta.somacom.controller.CartController.addCartItem(..)) && args(request, ..)")
    public void logAddToCart(JoinPoint joinPoint, CartItemAddRequest request) {
        getPrincipalDetails().ifPresent(principal -> {
            productRepository.findById(request.getProductId()).ifPresent(product -> {
                userIntentLoggingService.logAction(
                        String.valueOf(principal.getUser().getId()),
                        product.getBaseSpec().getId(),
                        UserActionType.CART
                );
            });
        });
    }

    /**
     * 상품 검색 및 필터 적용 시 'SEARCH', 'FILTER' 액션을 로깅합니다. (P-201-SEARCH)
     */
    @AfterReturning(pointcut = "execution(* com.kosta.somacom.controller.ProductSearchController.searchProducts(..)) && args(condition, ..)")
    public void logSearchAndFilter(JoinPoint joinPoint, ProductSearchCondition condition) {
        getPrincipalDetails().ifPresent(principal -> {
            String userId = String.valueOf(principal.getUser().getId());

            // 검색어 로깅
            if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
                // 키워드(모델명)로 BaseSpec을 찾아서, 해당 모델의 모든 호환성 태그에 대해 점수를 올립니다.
                baseSpecRepository.findFirstByNameIgnoreCase(condition.getKeyword()).ifPresent(baseSpec -> {
                    userIntentLoggingService.logAction(
                            userId,
                            baseSpec.getId(),
                            UserActionType.SEARCH
                    );
                });
            }

            // 필터 로깅
            if (condition.getFilters() != null) {
                for (Map.Entry<String, String> filter : condition.getFilters().entrySet()) {
                    // 필터 값(value)이 여러 개일 수 있으므로 콤마로 분리하여 각각 로깅
                    String[] values = filter.getValue().split(",");
                    for (String value : values) {
                        userIntentLoggingService.logTagAction(userId, filter.getKey(), value.trim(), UserActionType.FILTER);
                    }
                }
            }
        });
    }

    /**
     * 주문 완료 시 'PURCHASE' 액션을 로깅합니다. (P-501)
     * @param orderItems 생성된 주문에 포함된 아이템 목록
     */
    @AfterReturning(pointcut = "execution(* com.kosta.somacom.service.OrderService.createOrder(..))", returning = "orderItems")
    public void logPurchaseFromCart(JoinPoint joinPoint, List<OrderItem> orderItems) {
        logPurchase(orderItems);
    }

    /**
     * 즉시 구매로 주문 완료 시 'PURCHASE' 액션을 로깅합니다. (P-202)
     */
    @AfterReturning(pointcut = "execution(* com.kosta.somacom.service.OrderService.createInstantOrder(..))", returning = "orderItem")
    public void logPurchaseFromInstant(JoinPoint joinPoint, OrderItem orderItem) {
        logPurchase(Collections.singletonList(orderItem));
    }

    private void logPurchase(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) return;

        getPrincipalDetails().ifPresent(principal -> {
            for (OrderItem item : orderItems) {
                Product product = item.getProduct();
                userIntentLoggingService.logAction(
                        String.valueOf(principal.getUser().getId()),
                        product.getBaseSpec().getId(),
                        UserActionType.PURCHASE
                );
            }
        });
    }

    private java.util.Optional<PrincipalDetails> getPrincipalDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails) {
            return java.util.Optional.of((PrincipalDetails) authentication.getPrincipal());
        }
        return java.util.Optional.empty();
    }
}