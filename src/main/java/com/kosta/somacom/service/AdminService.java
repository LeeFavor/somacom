package com.kosta.somacom.service;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.admin.dto.UserManagementResponse;
import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;
import com.kosta.somacom.domain.user.UserStatus;
import com.kosta.somacom.dto.request.BaseSpecRequestProcessDto;
import com.kosta.somacom.dto.response.BaseSpecRequestResponseDto;
import com.kosta.somacom.repository.BaseSpecRequestRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final BaseSpecRequestRepository baseSpecRequestRepository;

    /**
     * A-203: PENDING 상태의 모델 등록 요청 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<BaseSpecRequestResponseDto> getPendingBaseSpecRequests(Pageable pageable) {
        // BaseSpecRequest와 Seller를 JOIN FETCH로 조회하며 페이징 처리
        Page<BaseSpecRequest> requests = baseSpecRequestRepository.findPendingRequestsWithSeller(pageable);
        // Page<BaseSpecRequest>를 Page<BaseSpecRequestResponseDto>로 변환
        return requests.map(BaseSpecRequestResponseDto::new);
    }

    /**
     * A-203: 모델 등록 요청 처리 (승인/거절)
     */
    @Transactional
    public void processBaseSpecRequest(Long requestId, BaseSpecRequestProcessDto dto) {
        BaseSpecRequest request = baseSpecRequestRepository.findById(requestId)
                .orElseThrow(() -> new EntityNotFoundException("요청을 찾을 수 없습니다: " + requestId));

        if (request.getStatus() != BaseSpecRequestStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 요청입니다.");
        }

        request.process(dto.getStatus(), dto.getAdminNotes(), LocalDateTime.now());
    }
    @Transactional(readOnly = true)
    public Page<SellerRequestDto> getSellerRequests(Pageable pageable) {
        Page<User> pendingSellers = userRepository.findByRoleWithSellerInfo(UserRole.SELLER_PENDING, pageable);
        return pendingSellers.map(SellerRequestDto::new);
    }

    @Transactional
    public void approveSellerRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.SELLER_PENDING) {
            throw new IllegalStateException("User is not a pending seller.");
        }

        user.setRole(UserRole.SELLER);
        // userRepository.save(user)는 @Transactional에 의해 더티 체킹(dirty checking)되므로 명시적으로 호출할 필요가 없습니다.
    }
    
    @Transactional
    public void suspendSellerRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        if (user.getRole() != UserRole.SELLER_PENDING) {
            throw new IllegalStateException("User is not a pending seller.");
        }

        user.updateStatus(UserStatus.SUSPENDED);
        // userRepository.save(user)는 @Transactional에 의해 더티 체킹(dirty checking)되므로 명시적으로 호출할 필요가 없습니다.
    }

    /**
     * A-102: 모든 회원/판매자 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<UserManagementResponse> getAllUsers(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.findByEmailLike("%" + keyword.trim() + "%", pageable);
        }
        return users.map(UserManagementResponse::new);
    }

    /**
     * A-102: 회원/판매자 계정 상태 변경
     */
    @Transactional
    public void updateUserStatus(Long userId, UserStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        user.updateStatus(status);
    }
    
    public Long findUsersCounts() {
    	return userRepository.count();
    }
}