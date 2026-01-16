package com.anafXsamsul.controller;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.dto.auth.AuthResponse;
import com.anafXsamsul.dto.auth.LoginRequest;
import com.anafXsamsul.dto.auth.LoginResponse;
import com.anafXsamsul.dto.auth.RegisterRequest;
import com.anafXsamsul.dto.auth.ResendOtpRequest;
import com.anafXsamsul.dto.auth.ResendOtpResponse;
import com.anafXsamsul.dto.auth.VerifyOtpRequest;
import com.anafXsamsul.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.status(200).body(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.status(200).body(apiResponse);
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<ResendOtpResponse>> resendOtp(@RequestBody @Valid ResendOtpRequest request) {

        ResendOtpResponse response = authService.resendOtp(request.getEmail());

        ApiResponse<ResendOtpResponse> apiResponse = ApiResponse.<ResendOtpResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        ApiResponse<LoginResponse> apiResponse = ApiResponse.<LoginResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.ok(apiResponse);
    }

    // Frontend akan langsung mengarahkan user ke endpoint OAuth2 di backend
    @GetMapping("/oauth2/url/google")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String authUrl = "http://localhost:8082/oauth2/authorization/google";

        return ResponseEntity.ok(Map.of(
                "url", authUrl,
                "method", "GET"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        return ResponseEntity.ok(Map.of(
                "message", "Logout successful",
                "timestamp", LocalDateTime.now()));
    }
}
