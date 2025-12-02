package com.kosta.somacom.controller;

import com.kosta.somacom.auth.dto.SellerSignupRequest;
import com.kosta.somacom.auth.dto.UserSignupRequest;
import com.kosta.somacom.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup/user")
    public ResponseEntity<Long> signupUser(@RequestBody UserSignupRequest request) {
        Long userId = authService.signupUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }
    
    @PostMapping("/signup/seller")
    public ResponseEntity<Long> signupSeller(@RequestBody SellerSignupRequest request) {
        Long userId = authService.signupSeller(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userId);
    }
}