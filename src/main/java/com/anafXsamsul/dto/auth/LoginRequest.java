package com.anafXsamsul.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginRequest {

    @NotBlank(message = "Email atau username is required")
    private String emailOrUsername;

    @NotBlank(message = "Password is required")
    private String password;
    
}
