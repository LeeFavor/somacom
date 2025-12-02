package com.kosta.somacom.service;

import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.user.dto.UserInfoResponse;
import com.kosta.somacom.repository.UserRepository;
import com.kosta.somacom.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        return new UserInfoResponse(user);
    }

    public void updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        // 닉네임 변경 로직
        if (StringUtils.hasText(request.getUsername()) && !user.getUsername().equals(request.getUsername())) {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
            }
            user.updateUsername(request.getUsername());
        }

        // 비밀번호 변경 로직
        if (StringUtils.hasText(request.getNewPassword())) {
            user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        }
    }

    public void deactivateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
        user.deactivate();
    }
}