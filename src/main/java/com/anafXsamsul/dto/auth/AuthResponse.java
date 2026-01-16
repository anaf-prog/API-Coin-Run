package com.anafXsamsul.dto.auth;

import java.time.LocalDateTime;
import com.anafXsamsul.entity.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private Long userId;
    private String username;
    private String email;
    private String phoneNumber;
    private Users.UserRole role;
    private Users.UserStatus status;
    private LocalDateTime createdAt;

}
