package com.anafXsamsul.dto;

import com.anafXsamsul.notation.ValidEmailDomain;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResetPasswordRequest {

    @NotBlank
    @ValidEmailDomain
    private String email;

    @NotBlank
    private String otp;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, message = "Password minimal 8 karakter")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).*$",
        message = "Password harus mengandung minimal satu huruf besar, satu huruf kecil, dan satu karakter khusus"
    )
    private String newPassword;

    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, message = "Password minimal 8 karakter")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).*$",
        message = "Password harus mengandung minimal satu huruf besar, satu huruf kecil, dan satu karakter khusus"
    )
    private String confirmNewPassword;
    
}
