package com.kosta.somacom.user.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String username; // 변경할 닉네임
    private String newPassword; // 변경할 새 비밀번호
}