package com.anafXsamsul.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendLoginNotification(String toEmail, String username, LocalDateTime loginTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Notifikasi Login Akun");
        message.setText(
                "Halo " + username + ",\n\n" +
                        "Kami mendeteksi login ke akun Anda.\n\n" +
                        "Waktu Login : " + loginTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n\n"
                        +
                        "Jika ini bukan Anda, segera ganti password dan hubungi admin.\n\n" +
                        "Salam,\n" +
                        "Tim Keamanan");

        try {
            mailSender.send(message);
            logger.info("Email login berhasil dikirim ke {}", toEmail);
        } catch (Exception e) {
            logger.error("Gagal kirim email login ke {} : {}", toEmail, e.getMessage());
        }
    }

    @Async("taskExecutor")
    public void sendOtpEmail(String toEmail, String username, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Kode OTP Registrasi");
        message.setText(
                "Halo " + username + ",\n\n" +
                        "Kode OTP registrasi Anda adalah:\n\n" +
                        otp + "\n\n" +
                        "OTP ini berlaku selama 5 menit.\n\n" +
                        "Jika Anda tidak merasa mendaftar, abaikan email ini.\n\n" +
                        "Salam,\nTim Security");
        
        try {
            mailSender.send(message);
            logger.info("Email login berhasil dikirim ke {}", toEmail);

        } catch (Exception e) {
            logger.error("Gagal kirim email login ke {} : {}", toEmail, e.getMessage());
        }
        
    }

    @Async("taskExecutor")
    public void sendPasswordResetNotification(String toEmail, String username, LocalDateTime resetTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Password Akun Berhasil Direset");
        message.setText(
                "Halo " + username + ",\n\n" +
                        "Password akun Anda berhasil direset.\n\n" +
                        "Waktu Reset : " +
                        resetTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) +
                        "\n\nJika Anda tidak merasa melakukan ini, segera hubungi admin.\n\n" +
                        "Salam,\nTim Keamanan");

        try {
            mailSender.send(message);
            logger.info("Email reset password berhasil dikirim ke {}", toEmail);
        } catch (Exception e) {
            logger.error("Gagal kirim email reset password ke {} : {}", toEmail, e.getMessage());
        }
    }

}
