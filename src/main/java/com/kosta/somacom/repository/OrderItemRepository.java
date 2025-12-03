package com.kosta.somacom.repository;

import com.kosta.somacom.domain.order.OrderItem;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query(value = "SELECT oi FROM OrderItem oi JOIN FETCH oi.product p JOIN FETCH oi.order o WHERE p.seller.id = :sellerId",
           countQuery = "SELECT count(oi) FROM OrderItem oi JOIN oi.product p WHERE p.seller.id = :sellerId")
    Page<OrderItem> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
    List<OrderItem> findByOrder_OrderedAtAfter(LocalDateTime orderedAt);

}