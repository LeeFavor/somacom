package com.kosta.somacom.service;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SellerRequestDto> getSellerRequests() {
        List<User> pendingSellers = userRepository.findByRoleWithSellerInfo(UserRole.SELLER_PENDING);
        return pendingSellers.stream()
                .map(SellerRequestDto::new)
                .collect(Collectors.toList());
    }

    public void approveSellerRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.SELLER_PENDING) {
            throw new IllegalStateException("User is not a pending seller.");
        }

        user.setRole(UserRole.SELLER);
        // userRepository.save(user)는 @Transactional에 의해 더티 체킹(dirty checking)되므로 명시적으로 호출할 필요가 없습니다.
    }
}