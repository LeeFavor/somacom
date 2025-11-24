package com.kosta.somacom.auth.dto;

import lombok.Data;

@Data
public class SellerSignupRequest {
    private String email;
    private String password;
    private String companyName;
    private String companyNumber;
    private String phoneNumber;
}
