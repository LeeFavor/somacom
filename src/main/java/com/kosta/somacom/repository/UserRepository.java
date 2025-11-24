package com.kosta.somacom.repository;

import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);
	Optional<User> findByUsername(String username);
	@Query("SELECT u FROM User u JOIN FETCH u.sellerInfo WHERE u.role = :role")
    List<User> findByRoleWithSellerInfo(@Param("role") UserRole role);
}