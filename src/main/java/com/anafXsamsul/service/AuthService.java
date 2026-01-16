package com.anafXsamsul.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.anafXsamsul.dto.auth.AuthResponse;
import com.anafXsamsul.dto.auth.LoginRequest;
import com.anafXsamsul.dto.auth.LoginResponse;
import com.anafXsamsul.dto.auth.RegisterRequest;
import com.anafXsamsul.dto.auth.ResendOtpResponse;
import com.anafXsamsul.dto.auth.VerifyOtpRequest;
import com.anafXsamsul.entity.UserProfile;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.AuthProvider;
import com.anafXsamsul.entity.Users.UserStatus;
import com.anafXsamsul.error.custom.EmailAlreadyExistException;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.error.custom.UserNameAlreadyExistException;
import com.anafXsamsul.repository.UserProfileRepository;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.security.CustomUserDetails;
import com.anafXsamsul.security.JwtService;
import com.anafXsamsul.utility.GenerateOtp;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private GenerateOtp generateOtp;

    @Autowired
    private EmailService emailService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistException();
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserNameAlreadyExistException();
        }

        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setStatus(Users.UserStatus.PENDING);
        user.setRole(Users.UserRole.USER);
        user.setCreatedAt(LocalDateTime.now().withNano(0));

        // === OTP ===
        String otp = generateOtp.generate();
        user.setOtpCode(otp);
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));
        user.setEmailVerified(false);

        Users savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);

        // Ekstrak nama depan dari email
        String email = savedUser.getEmail();
        String emailPart = email.split("@")[0];

        // Bersihkan karakter kusus dan angka
        String cleanName = emailPart.replaceAll("[^a-zA-Z]", " ");
        cleanName = cleanName.replaceAll("\\s+", " ").trim();

        // Pecah jadi kata kata
        String[] namePart = cleanName.split("\\s+");

        if (namePart.length >= 1) {
            profile.setFirstName(namePart[0]);
        }

        if (namePart.length >= 2) {
            profile.setLastName(namePart[1]);

        } else {
            profile.setFirstName(namePart[0]);
            profile.setLastName("");
        }

        profile.setUpdatedAt(LocalDateTime.now().withNano(0));

        userProfileRepository.save(profile);

        try {

            // Kirim OTP ke email
            emailService.sendOtpEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
                otp);

            log.info("Email OTP berhasil dikirim");    
            
        } catch (Exception e) {
            log.error("Gagal kirim email otp ke : {} karena {} ", user.getEmail(), e.getMessage());
        }

        return AuthResponse.builder()
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .phoneNumber(savedUser.getPhoneNumber())
            .role(savedUser.getRole())
            .status(savedUser.getStatus())
            .createdAt(savedUser.getCreatedAt())
        .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {

        Users user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new LoginEmailOrUsernameException("Email tidak terdaftar"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new LoginEmailOrUsernameException("Email tidak valid");
        }

        if (!request.getOtp().equals(user.getOtpCode())) {
            throw new LoginEmailOrUsernameException("OTP salah");
        }

        if (user.getOtpExpiredAt().isBefore(LocalDateTime.now())) {
            throw new LoginEmailOrUsernameException("OTP sudah expired");
        }

        user.setEmailVerified(true);
        user.setStatus(Users.UserStatus.ACTIVE);
        user.setOtpCode(null);
        user.setOtpExpiredAt(null);
        user.setUpdatedAt(LocalDateTime.now().withNano(0));

        Users savedUser = userRepository.save(user);

        return AuthResponse.builder()
            .userId(savedUser.getId())
            .username(savedUser.getUsername())
            .email(savedUser.getEmail())
            .phoneNumber(savedUser.getPhoneNumber())
            .role(savedUser.getRole())
            .status(savedUser.getStatus())
            .createdAt(savedUser.getCreatedAt())
        .build();
    }

    @Transactional
    public ResendOtpResponse resendOtp(String email) {
        Users user = userRepository.findByEmail(email)
            .orElseThrow(() -> new LoginEmailOrUsernameException("Email tidak terdaftar"));
        
        String newOtp = generateOtp.generate();
        LocalDateTime otpExpiry = LocalDateTime.now().withNano(0).plusMinutes(5);

        user.setOtpCode(newOtp);
        user.setOtpExpiredAt(otpExpiry);
        user.setUpdatedAt(LocalDateTime.now().withNano(0));
        userRepository.save(user);

        try {
            emailService.sendOtpEmail(
                user.getEmail(),
                user.getUsername(),
                newOtp);
            log.info("OTP berhasil dikirim ulang ke: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Gagal mengirim OTP ulang ke: {}, error: {}", user.getEmail(), e.getMessage());
            throw new LoginEmailOrUsernameException("Gagal mengirim OTP. Silakan coba lagi.");
        }

        return ResendOtpResponse.builder()
            .info("OTP berhasil dikirim")
            .otpSentAt(LocalDateTime.now().withNano(0))
            .otpExpiredAt(otpExpiry)
        .build();
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {

        Users user = userRepository.findByEmailOrUsername(request.getEmailOrUsername())
            .orElseThrow(() -> new LoginEmailOrUsernameException());

        if (user.getStatus() == UserStatus.CLOSED) {
            throw new LoginEmailOrUsernameException("Akun tidak ditemukan / akun telah dihapus");
        }
        
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new LoginEmailOrUsernameException("Akun anda dibekukan karena terdeteksi aktivitas mecurigakan, segera hubungi customer service untuk tindakan lebih lanjut");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new LoginEmailOrUsernameException("Proses registrasi belum selesai");
        }

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmailOrUsername(), // Kirim identifier (email atau username)
                request.getPassword()));

        // Get user dari authentication
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        user = userDetails.getUser();

        // Update last login
        user.setLastLoginAt(LocalDateTime.now().withNano(0));
        userRepository.save(user);

        // Generate token
        var jwtToken = jwtService.generateToken(userDetails);
        var refreshToken = jwtService.generateRefreshToken(userDetails);

        log.debug("Login berhasil");

        return LoginResponse.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .role(user.getRole())
            .status(user.getStatus())
            .kycStatus(user.getKycStatus())
            .createdAt(user.getCreatedAt())
            .provider(user.getProvider())
            .lastLoginAt(user.getLastLoginAt().withNano(0))
            .token(jwtToken)
            .refreshToken(refreshToken)
        .build();
    }

}
