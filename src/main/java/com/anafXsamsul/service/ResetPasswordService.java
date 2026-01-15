package com.anafXsamsul.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ResetPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ApiResponse<String> setInitialPassword(String email, String newPassword) {
        Users user = userRepository.findByEmail(email)
            .orElseThrow(() -> new LoginEmailOrUsernameException("User belum terverifikasi"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now().withNano(0));
        userRepository.save(user);
        
        logger.info("Password berhasil di update");
        
        return ApiResponse.<String>builder()
            .statusCode(200)
            .message("success")
            .data("Password updated")
        .build();
    }

    @Transactional
    public ApiResponse<String> userResetPassword(String email, String newPassword) {
        Users user = userRepository.findByEmail(email)
            .orElseThrow(() -> new LoginEmailOrUsernameException("User belum terverifikasi"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now().withNano(0));
        userRepository.save(user);
        
        logger.info("Password berhasil di update");

        emailService.notifyAllUsersAboutPasswordReset(email);

        return ApiResponse.<String>builder()
            .statusCode(200)
            .message("success")
            .data("Password updated")
        .build();
    }
    
}
