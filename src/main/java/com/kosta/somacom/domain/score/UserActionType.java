package com.kosta.somacom.domain.score;

import lombok.Getter;

@Getter
public enum UserActionType {
    VIEW("view_count"),
    LONG_VIEW("long_view_count"),
    IMAGE_VIEW("image_view_count"),
    SEARCH("search_count"),
    FILTER("filter_count"),
    WISHLIST("wishlist_count"),
    CART("cart_count"),
    PURCHASE("purchase_count");

    private final String columnName;

    UserActionType(String columnName) {
        this.columnName = columnName;
    }
}