package com.kosta.somacom.domain.score;

import lombok.Getter;

/**
 * 사용자 행동 유형별 가중치를 정의하는 Enum.
 * 추천 점수 계산 시 사용자의 의도 강도를 측정하는 데 사용됩니다.
 */
@Getter
public enum UserActionWeight {
    VIEW(UserActionType.VIEW, 1),
    LONG_VIEW(UserActionType.LONG_VIEW, 2),
    IMAGE_VIEW(UserActionType.IMAGE_VIEW, 2),
    SEARCH(UserActionType.SEARCH, 3),
    FILTER(UserActionType.FILTER, 4),
    WISHLIST(UserActionType.WISHLIST, 5),
    CART(UserActionType.CART, 7),
    PURCHASE(UserActionType.PURCHASE, 10);

    private final int weight;

    UserActionWeight(UserActionType actionType, int weight) {
        this.weight = weight;
    }
}