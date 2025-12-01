package com.kosta.somacom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.kosta.somacom.domain.cart.Cart;
import com.kosta.somacom.domain.part.BaseSpec;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findByUserId(Long userId);
    @Query("SELECT ci.product.baseSpec FROM CartItem ci WHERE ci.cart.user.id = :userId")
    List<BaseSpec> findBaseSpecsInCartByUserId(@Param("userId") Long userId);
}