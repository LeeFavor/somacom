package com.kosta.somacom.service;

import com.kosta.somacom.admin.dto.SellerRequestDto;
import com.kosta.somacom.domain.request.BaseSpecRequest;
import com.kosta.somacom.domain.request.BaseSpecRequestStatus;
import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;
import com.kosta.somacom.dto.request.BaseSpecRequestProcessDto;
import com.kosta.somacom.dto.response.BaseSpecRequestResponseDto;
import com.kosta.somacom.repository.BaseSpecRequestRepository;
import com.kosta.somacom.repository.UserRepository;
import lombok.RequiredArgsConstructor;
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
    public List<BaseSpecRequestResponseDto> getPendingBaseSpecRequests() {
        // 1. BaseSpecRequest와 Seller를 먼저 조회
        List<BaseSpecRequest> requests = baseSpecRequestRepository.findPendingRequestsWithSeller();

        if (requests.isEmpty()) {
            return List.of();
        }

        // 2. 위에서 조회한 Seller들의 SellerInfo를 별도로 조회 (N+1 방지)
        List<User> sellersToFetch = requests.stream().map(BaseSpecRequest::getSeller).distinct().collect(Collectors.toList());
        List<User> sellersWithInfo = userRepository.findWithSellerInfoIn(sellersToFetch);

        // 3. Seller ID를 키로 하는 Map을 생성하여 쉽게 찾을 수 있도록 함
        Map<Long, User> sellerInfoMap = sellersWithInfo.stream()
                .collect(Collectors.toMap(User::getId, user -> user));

        return requests.stream()
                .map(BaseSpecRequestResponseDto::new)
                .collect(Collectors.toList());
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
    public List<SellerRequestDto> getSellerRequests() {
        List<User> pendingSellers = userRepository.findByRoleWithSellerInfo(UserRole.SELLER_PENDING);
        return pendingSellers.stream()
                .map(SellerRequestDto::new)
                .collect(Collectors.toList());
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
}