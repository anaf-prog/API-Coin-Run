package com.anafXsamsul.dto;

import com.anafXsamsul.notation.ValidEmailDomain;

import jakarta.validation.constraints.Email;
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
public class RegisterRequest {
    
    @NotBlank(message = "Email wajib diisi")
    @Email(message = "Email tidak valid")
    @ValidEmailDomain
    private String email;
    
    @NotBlank(message = "Password wajib diisi")
    @Size(min = 8, message = "Password minimal 8 karakter")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).*$",
        message = "Password harus mengandung minimal satu huruf besar, satu huruf kecil, dan satu karakter khusus"
    )
    private String password;

    
    private String username;
    
    @NotBlank(message = "Nomor telepon wajib diisi")
    @Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Nomor telepon harus terdiri dari 10-15 digit angka dan boleh diawali tanda + untuk kode negara"
    )
    private String phoneNumber;
}
