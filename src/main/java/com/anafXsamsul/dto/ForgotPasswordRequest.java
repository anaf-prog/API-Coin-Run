package com.anafXsamsul.dto;

import com.anafXsamsul.notation.ValidEmailDomain;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ForgotPasswordRequest {

    @NotBlank
    @ValidEmailDomain
    private String email;
    
}
