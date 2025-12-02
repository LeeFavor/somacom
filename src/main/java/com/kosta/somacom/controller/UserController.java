package com.kosta.somacom.controller;

import com.kosta.somacom.auth.PrincipalDetails;
import com.kosta.somacom.service.UserService;
import com.kosta.somacom.user.dto.UserInfoResponse;
import com.kosta.somacom.user.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        UserInfoResponse userInfo = userService.getUserInfo(principalDetails.getUser().getId());
        return ResponseEntity.ok(userInfo);
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateUser(@RequestBody UserUpdateRequest request,
                                           @AuthenticationPrincipal PrincipalDetails principalDetails) {
        userService.updateUser(principalDetails.getUser().getId(), request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deactivateUser(@AuthenticationPrincipal PrincipalDetails principalDetails) {
        userService.deactivateUser(principalDetails.getUser().getId());
        // 탈퇴 후에는 토큰이 무효화되므로 클라이언트에서 로그아웃 처리가 필요합니다.
        return ResponseEntity.noContent().build();
    }
}