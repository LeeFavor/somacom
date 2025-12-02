package com.kosta.somacom.domain.order;

import com.kosta.somacom.domain.user.User;
import javax.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(nullable = false)
    private String recipientName;

    @Lob
    @Column(nullable = false)
    private String shippingAddress;

    @Column(length = 20)
    private String shippingPostcode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreationTimestamp
    @Column(name = "ordered_at", updatable = false)
    private LocalDateTime orderedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    public Order(User user, BigDecimal totalPrice, String recipientName, String shippingAddress, String shippingPostcode, OrderStatus status) {
        this.user = user;
        this.totalPrice = totalPrice;
        this.recipientName = recipientName;
        this.shippingAddress = shippingAddress;
        this.shippingPostcode = shippingPostcode;
        this.status = status;
    }

    //== 연관관계 편의 메소드 ==//
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}