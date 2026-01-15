package com.anafXsamsul.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetSuccessEvent {

    private String email;
    private String username;
    private LocalDateTime resetTime;
    
}

