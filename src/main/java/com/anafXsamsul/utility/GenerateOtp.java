package com.anafXsamsul.utility;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class GenerateOtp {
    private final SecureRandom random = new SecureRandom();

    public String generate() {
        int timePart = (int) (System.nanoTime() % 100);
        int randomPart = random.nextInt(100);
        int uuidPart = Math.abs(UUID.randomUUID().hashCode() % 100);

        return String.format("%02d%02d%02d", timePart, randomPart, uuidPart);
    }
}
