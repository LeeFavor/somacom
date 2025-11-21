package com.kosta.somacom.repository;

import com.kosta.somacom.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}