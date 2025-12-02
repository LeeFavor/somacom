package com.kosta.somacom.admin.dto;

import com.kosta.somacom.domain.user.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SellerRequestDto {
    private Long userId;
    private String email;
    private String companyName;
    private String companyNumber;
    private String phoneNumber;
    private LocalDateTime requestedAt;

    public SellerRequestDto(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.requestedAt = user.getCreatedAt();
        this.companyName = user.getSellerInfo().getCompanyName();
        this.companyNumber = user.getSellerInfo().getCompanyNumber();
        this.phoneNumber = user.getSellerInfo().getPhoneNumber();
    }
}