package com.anafXsamsul.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${spring.mail.password}")
    private String brevoApiKey;

    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${app.mail.from}")
    private String fromEmail;

    private void sendViaBrevoApi(String toEmail, String subject, String textContent) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            Map<String, Object> body = new HashMap<>();
            
            // Sender
            Map<String, String> sender = new HashMap<>();
            sender.put("name", "No Reply Coin Run");
            sender.put("email", fromEmail);
            body.put("sender", sender);

            // To
            Map<String, String> receiver = new HashMap<>();
            receiver.put("email", toEmail);
            body.put("to", Collections.singletonList(receiver));

            body.put("subject", subject);
            body.put("textContent", textContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(BREVO_API_URL, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email '{}' berhasil dikirim", subject);
            } else {
                log.error("Error : {}", response.getBody());
            }
        } catch (Exception e) {
            log.error("Gagal kirim email: {}", e.getMessage());
        }
    }

    public void sendLoginNotification(String toEmail, String username, LocalDateTime loginTime) {
        String subject = "Notifikasi Login Akun";
        String content = "Halo " + username + ",\n\n" +
                "Kami mendeteksi login ke akun Anda.\n\n" +
                "Waktu Login : " + loginTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n\n" +
                "Jika ini bukan Anda, segera ganti password dan hubungi admin.\n\n" +
                "Salam,\nTim Keamanan";

        sendViaBrevoApi(toEmail, subject, content);
    }

    @Async("taskExecutor")
    public void sendOtpEmail(String toEmail, String username, String otp) {
        String subject = "Kode OTP Registrasi";
        String content = "Halo " + username + ",\n\n" +
                "Kode OTP registrasi Anda adalah:\n\n" +
                otp + "\n\n" +
                "OTP ini berlaku selama 5 menit.\n\n" +
                "Jika Anda tidak merasa mendaftar, abaikan email ini.\n\n" +
                "Salam,\nTim Security";

        sendViaBrevoApi(toEmail, subject, content);
    }

    @Async("taskExecutor")
    public void sendPasswordResetNotification(String toEmail, String username, LocalDateTime resetTime) {
        String subject = "Password Akun Berhasil Direset";
        String content = "Halo " + username + ",\n\n" +
                "Password akun Anda berhasil direset.\n\n" +
                "Waktu Reset : " +
                resetTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) +
                "\n\nJika Anda tidak merasa melakukan ini, segera hubungi admin.\n\n" +
                "Salam,\nTim Keamanan";

        sendViaBrevoApi(toEmail, subject, content);
    }

}
