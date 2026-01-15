package com.anafXsamsul.notation;

import java.util.Arrays;
import java.util.List;
import jakarta.validation.ConstraintValidator;

public class EmailDomainValidator implements ConstraintValidator<ValidEmailDomain, String> {

    // Daftar domain yang diizinkan
    private static final List<String> ALLOWED_DOMAINS = Arrays.asList(
        "gmail.com",
        "outlook.com",
        "icloud.com",
        "yahoo.com",
        "yahoo.co.id"
    );

    @Override
    public boolean isValid(String email, jakarta.validation.ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Biarkan @NotBlank yang menangani jika kosong
        }

        // Ambil bagian setelah tanda @
        if (!email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.lastIndexOf("@") + 1).toLowerCase();
        return ALLOWED_DOMAINS.contains(domain);
    }
    
}
