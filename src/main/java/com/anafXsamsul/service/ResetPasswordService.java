package com.anafXsamsul.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.dto.resetpassword.PasswordResetSuccessEvent;
import com.anafXsamsul.dto.resetpassword.ResetPasswordRequest;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.utility.GenerateOtp;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ResetPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GenerateOtp generateOtp;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Transactional
    public ApiResponse<String> requestResetPassword(String email) {
        Users user = userRepository.findByEmail(email)
            .orElseThrow(() -> new LoginEmailOrUsernameException("Email tidak terdaftar"));

        String otp = generateOtp.generate();
        user.setOtpCode(otp);
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));
        user.setUpdatedAt(LocalDateTime.now().withNano(0));
        userRepository.save(user);

        try {

            emailService.sendOtpEmail(user.getEmail(), user.getUsername(), otp);

            log.info("OTP reset password telah dikirim ke email: {}", email);
            
        } catch (Exception e) {
            log.error("Gagal kirim email otp : {} ", e.getMessage());
        }

        return ApiResponse.<String>builder()
            .statusCode(200)
            .message("success")
            .data("OTP reset password telah dikirim ke email")
        .build();
    }

    @Transactional
    public ApiResponse<String> resetPassword(ResetPasswordRequest request) {

        Users user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new LoginEmailOrUsernameException("Email tidak terdaftar"));

        // Cek status akun
        if (user.getStatus() != Users.UserStatus.ACTIVE) {
            throw new LoginEmailOrUsernameException("Akun belum aktif");
        }

        // Cek OTP
        if (user.getOtpCode() == null || !user.getOtpCode().equals(request.getOtp())) {
            throw new LoginEmailOrUsernameException("OTP salah");
        }

        // Cek expired OTP
        if (user.getOtpExpiredAt() == null ||
                user.getOtpExpiredAt().isBefore(LocalDateTime.now())) {
            throw new LoginEmailOrUsernameException("OTP sudah expired");
        }

        // Cek password & confirm password
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new LoginEmailOrUsernameException("Password dan konfirmasi password tidak sama");
        }

        // cegah password sama dengan sebelumnya
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new LoginEmailOrUsernameException("Password baru tidak boleh sama dengan password lama");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        user.setUpdatedAt(LocalDateTime.now().withNano(0));

        userRepository.save(user);

        eventPublisher.publishEvent(
            new PasswordResetSuccessEvent(
                user.getEmail(),
                user.getUsername(),
                LocalDateTime.now()
            )
        );

        log.info("Password berhasil direset untuk email: {}", user.getEmail());

        return ApiResponse.<String>builder()
            .statusCode(200)
            .message("success")
            .data("Password berhasil direset")
        .build();
    }
    
}
