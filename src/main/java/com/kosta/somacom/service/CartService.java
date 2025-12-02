package com.kosta.somacom.service;

import com.kosta.somacom.cart.dto.CartItemAddRequest;
import com.kosta.somacom.cart.dto.CartItemResponse;
import com.kosta.somacom.domain.cart.Cart;
import com.kosta.somacom.domain.cart.CartItem;
import com.kosta.somacom.domain.part.BaseSpec;
import com.kosta.somacom.domain.product.Product;
import com.kosta.somacom.domain.score.CompatibilityStatus;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.dto.response.CartItemDto;
import com.kosta.somacom.dto.response.CartResponse;
import com.kosta.somacom.engine.RuleEngineService;
import com.kosta.somacom.engine.rule.CompatibilityResult;
import com.kosta.somacom.repository.CartItemRepository;
import com.kosta.somacom.repository.CartRepository;
import com.kosta.somacom.repository.ProductRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final RuleEngineService ruleEngineService;

    

    /**
     * 장바구니에 담긴 아이템들의 호환성을 검사하는 내부 메소드
     */
    private CompatibilityResult checkCartCompatibility(List<CartItem> items) {
        if (items == null || items.size() < 2) {
            return CompatibilityResult.success(); // 아이템이 2개 미만이면 검사할 필요 없음
        }

        List<BaseSpec> baseSpecsInCart = items.stream()
                .map(item -> item.getProduct().getBaseSpec())
                .distinct() // 동일한 기반 모델은 한 번만 검사
                .collect(Collectors.toList());

        CompatibilityResult finalResult = CompatibilityResult.success();

        // 모든 부품 조합(N*N)에 대해 검사
        for (int i = 0; i < baseSpecsInCart.size(); i++) {
            for (int j = i + 1; j < baseSpecsInCart.size(); j++) {
                BaseSpec partA = baseSpecsInCart.get(i);
                BaseSpec partB = baseSpecsInCart.get(j);

                CompatibilityResult result = ruleEngineService.checkCompatibility(partA, partB);

                // 가장 심각한 결과를 채택
                if (result.getStatus() == CompatibilityStatus.FAIL) {
                    return result; // FAIL 발견 시 즉시 반환
                }
                if (result.getStatus() == CompatibilityStatus.WARN) {
                    finalResult = result;
                }
            }
        }

        return finalResult;
    }
    public void addCartItem(Long userId, CartItemAddRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = Cart.createCart(user);
            cartRepository.save(cart);
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        CartItem savedCartItem = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), product.getId());

        if (savedCartItem != null) {
            // 이미 장바구니에 있는 상품이면 수량만 증가
            savedCartItem.addQuantity(request.getQuantity());
        } else {
            // 없는 상품이면 새로 추가
            CartItem cartItem = CartItem.createCartItem(product, request.getQuantity());
            cart.addCartItem(cartItem);
        }
        // 변경 감지(Dirty Checking)에 의해 cartItem의 변경사항이 자동으로 DB에 반영됨
    }
    /**
     * U-301, U-302: 장바구니 조회 및 실시간 호환성 검사
     */
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId);
        // [수정] findByUserId가 Optional<Cart>가 아닌 Cart를 반환하므로, null 체크로 변경합니다.
        if (cart == null) {
            throw new EntityNotFoundException("Cart not found for user: " + userId);
        }

        List<CartItemDto> itemDtos = cart.getCartItems().stream()
                .map(CartItemDto::new)
                .collect(Collectors.toList());

        // 실시간 호환성 검사 로직
        CompatibilityResult finalResult = checkCartCompatibility(cart.getCartItems());

        return new CartResponse(itemDtos, finalResult);
    }
//    @Transactional(readOnly = true)
//    public CartResponse getCart(Long userId) {
//        Cart cart = cartRepository.findByUserId(userId);
//        if (cart == null) {
//            // 장바구니가 비어있는 경우
//            return new CartResponse(List.of(), "NOT_APPLICABLE", "장바구니가 비어있습니다.");
//        }
//
//        List<CartItemResponse> cartItems = cart.getCartItems().stream()
//                .map(CartItemResponse::new)
//                .collect(Collectors.toList());
//
//        // TODO: SYS-1 호환성 엔진 호출
//        // CompatibilityResult result = compatibilityService.check(cart.getCartItems());
//        // return new CartResponse(cartItems, result.getStatus(), result.getMessage());
//
//        return new CartResponse(cartItems, "CHECK_PENDING", "호환성 검사 대기 중입니다.");
//    }

    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found"));
        cartItem.setQuantity(quantity);
    }

    public void deleteCartItem(Long cartItemId, Long userId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new EntityNotFoundException("CartItem not found: " + cartItemId));

        if (!cartItem.getCart().getUser().getId().equals(userId)) {
            throw new SecurityException("You do not have permission to delete this item.");
        }
        cartItemRepository.deleteById(cartItemId);
    }

    public void deleteCartItems(List<Long> cartItemIds, Long userId) {
        List<CartItem> itemsToDelete = cartItemRepository.findAllById(cartItemIds);

        for (CartItem item : itemsToDelete) {
            if (!item.getCart().getUser().getId().equals(userId)) {
                throw new SecurityException("You do not have permission to delete item: " + item.getId());
            }
        }

        // 성능을 위해 일괄 삭제 실행
        cartItemRepository.deleteAllInBatch(itemsToDelete);
    }
}