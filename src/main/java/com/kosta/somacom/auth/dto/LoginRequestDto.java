package com.kosta.somacom.auth.dto;

import lombok.Data;

@Data
public class LoginRequestDto {
    private String username; // 클라이언트가 보내는 email
    private String password;
}