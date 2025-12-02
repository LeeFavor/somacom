package com.kosta.somacom.domain.cart;

import com.kosta.somacom.domain.product.Product;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cart_items")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_item_id")
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Setter
    @Column(nullable = false)
    private int quantity;

    @CreationTimestamp
    @Column(name = "added_at", updatable = false)
    private LocalDateTime addedAt;

    public static CartItem createCartItem(Product product, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.product = product;
        cartItem.setQuantity(quantity);
        return cartItem;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }
}