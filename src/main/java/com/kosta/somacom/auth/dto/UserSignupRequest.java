package com.kosta.somacom.auth.dto;

import lombok.Data;

@Data
public class UserSignupRequest {
    private String email;
    private String password;
    private String username; // 닉네임
}