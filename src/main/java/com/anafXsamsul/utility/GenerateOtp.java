package com.anafXsamsul.utility;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class GenerateOtp {
    private final SecureRandom random = new SecureRandom();
   
    public String generate() {
        return String.format("%06d", random.nextInt(1000000));
    }
}
