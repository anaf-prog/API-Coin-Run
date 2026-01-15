package com.anafXsamsul.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.anafXsamsul.dto.AuthResponse;
import com.anafXsamsul.dto.LoginRequest;
import com.anafXsamsul.dto.LoginResponse;
import com.anafXsamsul.dto.RegisterRequest;
import com.anafXsamsul.dto.VerifyOtpRequest;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.AuthProvider;
import com.anafXsamsul.error.custom.EmailAlreadyExistException;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.error.custom.UserNameAlreadyExistException;
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
    public LoginResponse login(LoginRequest request) {

        Users user = userRepository.findByEmailOrUsername(request.getEmailOrUsername())
            .orElseThrow(() -> new LoginEmailOrUsernameException());

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
