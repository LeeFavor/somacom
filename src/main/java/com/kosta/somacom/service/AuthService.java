package com.kosta.somacom.service;

import com.kosta.somacom.auth.dto.SellerSignupRequest;
import com.kosta.somacom.auth.dto.UserSignupRequest;
import com.kosta.somacom.domain.user.AuthProvider;
import com.kosta.somacom.domain.user.SellerInfo;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;
import com.kosta.somacom.domain.user.UserStatus;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public Long signupSeller(SellerSignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }

        // 1. User 정보 생성 (role: SELLER_PENDING)
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getCompanyName()) // 판매자는 닉네임을 상호명으로 사용
                .role(UserRole.SELLER_PENDING)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .build();

        // 2. SellerInfo 정보 생성
        SellerInfo sellerInfo = SellerInfo.builder()
                .user(user) // User 객체 연결
                .companyName(request.getCompanyName())
                .companyNumber(request.getCompanyNumber())
                .phoneNumber(request.getPhoneNumber())
                .build();

        user.setSellerInfo(sellerInfo); // User에 SellerInfo 연결
        return userRepository.save(user).getId();
    }
    public Long signupUser(UserSignupRequest request) {
        // 이메일 또는 닉네임 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .provider(AuthProvider.LOCAL)
                .build();

        User savedUser = userRepository.save(user);
        return savedUser.getId();
    }
}