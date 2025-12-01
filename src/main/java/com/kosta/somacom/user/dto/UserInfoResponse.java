package com.kosta.somacom.user.dto;

import com.kosta.somacom.domain.user.User;
import lombok.Data;

@Data
public class UserInfoResponse {
    private String email;
    private String username;

    public UserInfoResponse(User user) {
        this.email = user.getEmail();
        this.username = user.getUsername();
    }
}