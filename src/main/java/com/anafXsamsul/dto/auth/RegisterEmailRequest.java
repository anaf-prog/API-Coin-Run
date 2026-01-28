package com.anafXsamsul.dto.auth;

import com.anafXsamsul.notation.ValidEmailDomain;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterEmailRequest {

    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Email tidak valid")
    @ValidEmailDomain
    private String email;
}
