package com.kosta.somacom.aop;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.domain.order.OrderItem;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.score.UserActionType;
import com.kosta.somacom.dto.request.ProductSearchCondition;
import com.kosta.somacom.order.dto.InstantOrderRequest;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.service.UserIntentLoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Collections;
import java.util.List;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class UserActionLoggingAspect {

    private final UserIntentLoggingService userIntentLoggingService;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    /**
     * 상품 상세 페이지 조회 시 'VIEW' 액션을 로깅합니다. (P-202)
     */
    @AfterReturning(pointcut = "execution(* com.kosta.somacom.controller.ProductDetailController.getProductDetail(..)) && args(productId)", returning = "response")
    public void logProductView(JoinPoint joinPoint, Long productId, ResponseEntity<?> response) {
        getPrincipalDetails().ifPresent(principal -> {
            productRepository.findById(productId).ifPresent(product -> {
                userIntentLoggingService.logAction(
                        String.valueOf(principal.getUser().getId()),
                        product.getBaseSpec().getId(),
                        UserActionType.VIEW
                );
            });
        });
    }

    /**
     * 장바구니에 상품 추가 시 'CART' 액션을 로깅합니다. (P-301)
     */
    @AfterReturning("execution(* com.kosta.somacom.controller.CartController.addItemToCart(..)) && args(request, ..)")
    public void logAddToCart(JoinPoint joinPoint, CartItemAddRequest request) {
        getPrincipalDetails().ifPresent(principal -> {
            productRepository.findById(request.getProductId()).ifPresent(product -> { // 오타 수정: userIntentLogging-service -> userIntentLoggingService
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
                userIntentLoggingService.logTagAction(userId, "search_keyword", condition.getKeyword(), UserActionType.SEARCH);
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