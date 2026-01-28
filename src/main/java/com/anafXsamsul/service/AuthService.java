package com.anafXsamsul.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.CookieValue;
import com.anafXsamsul.dto.auth.AuthResponse;
import com.anafXsamsul.dto.auth.LoginRequest;
import com.anafXsamsul.dto.auth.LoginResponse;
import com.anafXsamsul.dto.auth.RegisterEmailRequest;
import com.anafXsamsul.dto.auth.RegisterEmailResponse;
import com.anafXsamsul.dto.auth.RegisterRequest;
import com.anafXsamsul.dto.auth.ResendOtpResponse;
import com.anafXsamsul.dto.auth.VerifyOtpRequest;
import com.anafXsamsul.entity.UserProfile;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.AuthProvider;
import com.anafXsamsul.entity.Users.UserStatus;
import com.anafXsamsul.error.custom.BusinessException;
import com.anafXsamsul.error.custom.EmailAlreadyExistException;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.error.custom.UserNameAlreadyExistException;
import com.anafXsamsul.repository.UserProfileRepository;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.security.CustomUserDetails;
import com.anafXsamsul.security.JwtService;
import com.anafXsamsul.utility.GenerateOtp;
import jakarta.servlet.http.HttpServletResponse;
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
    public RegisterEmailResponse registerEmail(RegisterEmailRequest request, HttpServletResponse response) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistException("Email sudah terdaftar");
        }

        String newOtp = generateOtp.generate();
        String otpToken = UUID.randomUUID().toString();
        LocalDateTime otpExpiry = LocalDateTime.now().withNano(0).plusMinutes(5);

        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.PENDING);
        user.setCreatedAt(LocalDateTime.now().withNano(0));

        // === OTP ===
        user.setOtpCode(newOtp);
        user.setOtpToken(otpToken);
        user.setOtpExpiredAt(otpExpiry);
        user.setEmailVerified(false);

        Users savedUser = userRepository.save(user);

        try {

            // Kirim OTP ke email
            emailService.sendOtpEmail(
                savedUser.getEmail(),
                savedUser.getUsername(),
            newOtp);

            log.info("Email OTP berhasil dikirim");

        } catch (Exception e) {
            log.error("Gagal kirim email otp ke : {} karena {} ", user.getEmail(), e.getMessage());
        }

        if (otpToken == null) {
            throw new LoginEmailOrUsernameException("OTP token tidak ditemukan");
        }

        ResponseCookie cookie = ResponseCookie.from("OTP_TOKEN", otpToken)
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMinutes(5))
            .sameSite("Strict")
        .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return RegisterEmailResponse.builder()
            .info("OTP berhasil dikirim")
            .otpSentAt(LocalDateTime.now().withNano(0))
            .otpExpiredAt(otpExpiry)
        .build();

    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request,  @CookieValue("OTP_TOKEN") String otpToken) {

        Users user = userRepository.findByOtpToken(otpToken)
            .orElseThrow(() -> new LoginEmailOrUsernameException("OTP tidak valid"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new LoginEmailOrUsernameException("Email sudah terverifikasi");
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
    public AuthResponse register(RegisterRequest request, @CookieValue(value = "OTP_TOKEN", required = false) String otpToken) {

        Users user = userRepository.findByOtpToken(otpToken)
            .orElseThrow(() -> new BusinessException("Session registrasi tidak valid"));

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserNameAlreadyExistException("Username sudah terdaftar");
        }    

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BusinessException("Email belum diverifikasi");
        }

        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setProvider(AuthProvider.LOCAL);
        user.setRole(Users.UserRole.USER);
        user.setUpdatedAt(LocalDateTime.now().withNano(0));

        user.setOtpToken(null);

        Users savedUser = userRepository.save(user);

        // ===== PROFILE =====
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);

        String emailPart = savedUser.getEmail().split("@")[0];
        String cleanName = emailPart.replaceAll("[^a-zA-Z]", " ")
            .replaceAll("\\s+", " ")
            .trim();

        String[] namePart = cleanName.split("\\s+");
        profile.setFirstName(namePart[0]);
        profile.setLastName(namePart.length > 1 ? namePart[1] : "");
        profile.setUpdatedAt(LocalDateTime.now().withNano(0));

        userProfileRepository.save(profile);

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
