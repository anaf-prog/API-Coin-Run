package com.anafXsamsul.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.dto.SetPasswordRequest;
import com.anafXsamsul.service.ResetPasswordService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class ResetPasswordController {

    @Autowired
    private ResetPasswordService resetPasswordService;

    // Endpoint untuk set password pertama kali (untuk social login users)
    @PostMapping("/set-password")
    public ResponseEntity<ApiResponse<String>> setPassword(@Valid @RequestBody SetPasswordRequest request) {
        
        ApiResponse<String> response = resetPasswordService.setInitialPassword(
            request.getEmail(), 
            request.getPassword()
        );
        
        return ResponseEntity.ok(response);
    }

    // Endpoint untuk reset password global
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@Valid @RequestBody SetPasswordRequest request) {
        
        ApiResponse<String> response = resetPasswordService.userResetPassword(
            request.getEmail(), 
            request.getPassword()
        );
        
        return ResponseEntity.ok(response);
    }
    
}
