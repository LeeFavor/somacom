package com.kosta.somacom.repository;

import com.kosta.somacom.domain.order.Order;
import com.kosta.somacom.domain.order.OrderStatus;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query(value = "select distinct o from Order o join fetch o.orderItems oi join fetch oi.product where o.user.id = :userId",
           countQuery = "select count(o) from Order o where o.user.id = :userId")
    Page<Order> findOrdersByUserId(@Param("userId") Long userId, Pageable pageable);
    
    @Query("select o from Order o join fetch o.orderItems oi join fetch oi.product where o.id = :orderId and o.user.id = :userId")
    Optional<Order> findOrderDetails(@Param("orderId") Long orderId, @Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.payment_order_id = :paymentOrderId AND o.status = :status")
    Optional<Order> findByPaymentOrderIdAndStatus(@Param("paymentOrderId") String paymentOrderId, @Param("status") OrderStatus status);

}