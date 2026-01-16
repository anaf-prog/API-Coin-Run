package com.anafXsamsul.dto.auth;

import java.time.LocalDateTime;
import com.anafXsamsul.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private Long userId;
    private String username;
    private String email;
    private Users.UserStatus status;
    private Users.KycStatus kycStatus;
    private Users.UserRole role;
    private LocalDateTime createdAt;
    private Users.AuthProvider provider;
    private LocalDateTime lastLoginAt;
    private String token;
    private String refreshToken;
    
}
