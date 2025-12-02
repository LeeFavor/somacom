package com.kosta.somacom.admin.dto;

import com.kosta.somacom.domain.user.User;
import com.kosta.somacom.domain.user.UserRole;
import com.kosta.somacom.domain.user.UserStatus;
import lombok.Getter;

@Getter
public class UserManagementResponse {

    private Long userId;
    private String email;
    private String username;
    private UserRole role;
    private UserStatus status;

    public UserManagementResponse(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.status = user.getStatus();
    }
}