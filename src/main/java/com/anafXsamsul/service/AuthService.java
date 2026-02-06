package com.anafXsamsul.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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
import com.anafXsamsul.entity.LoginHistory;
import com.anafXsamsul.entity.UserProfile;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.AuthProvider;
import com.anafXsamsul.entity.Users.UserStatus;
import com.anafXsamsul.error.custom.BusinessException;
import com.anafXsamsul.error.custom.EmailAlreadyExistException;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.error.custom.UserNameAlreadyExistException;
import com.anafXsamsul.repository.LoginHistoryRepository;
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
    private LoginHistoryRepository loginHistoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private GenerateOtp generateOtp;

    @Autowired
    private EmailService emailService;

    @Transactional
    public RegisterEmailResponse registerEmail(RegisterEmailRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistException("Email sudah terdaftar");
        }

        String newOtp = generateOtp.generate();
        String otpToken = UUID.randomUUID().toString();
        LocalDateTime otpExpiry = LocalDateTime.now().withNano(0).plusMinutes(5);

        Users user = new Users();
        user.setEmail(request.getEmail());
        user.setStatus(UserStatus.REGISTER);
        user.setCreatedAt(LocalDateTime.now().withNano(0));

        // === OTP ===
        user.setOtpCode(newOtp);
        user.setOtpToken(otpToken);
        user.setOtpExpiredAt(otpExpiry);
        user.setEmailVerified(false);

        Users savedUser = userRepository.save(user);

        log.debug("Otp Token buat cookie : " + otpToken);

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

        return RegisterEmailResponse.builder()
            .info("OTP berhasil dikirim")
            .otpSentAt(LocalDateTime.now().withNano(0))
            .otpExpiredAt(otpExpiry)
            .otpToken(otpToken)
        .build();

    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request, @CookieValue(value = "OTP_TOKEN", required = false) String otpToken) {

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
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {

        Users user = userRepository.findByEmailOrUsername(request.getEmailOrUsername())
            .orElseThrow(() -> new LoginEmailOrUsernameException());

        log.info("Loaded user - ID: {}, Email: {}, FailedAttempts: {}, LockedUntil: {}",
                user.getId(), user.getEmail(), user.getFailedAttempts(), user.getLockedUntil());

        if (user.getStatus() == UserStatus.CLOSED) {
            saveLoginHistory(user, ip, userAgent, false, "Account close");
            throw new LoginEmailOrUsernameException("Akun tidak ditemukan / akun telah dihapus");
        }

        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new LoginEmailOrUsernameException("Akun anda dibekukan.");
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new LoginEmailOrUsernameException("Proses registrasi belum selesai");
        }

        log.info("IP user yang login : {}", ip);

        user = userRepository.findById(user.getId()).orElseThrow(() -> new LoginEmailOrUsernameException());

        // Cek lock
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            long minutesLeft = Duration.between(LocalDateTime.now(), user.getLockedUntil()).toMinutes();

            throw new LoginEmailOrUsernameException("Akun terkunci. Coba lagi dalam " + minutesLeft + " menit");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmailOrUsername(), request.getPassword()));

            user = userRepository.findById(user.getId()).orElseThrow();
            user.setLastLoginAt(LocalDateTime.now().withNano(0));
            user.setFailedAttempts(0);
            userRepository.saveAndFlush(user);

            saveLoginHistory(user, ip, userAgent, true, null);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .role(user.getRole())
                .status(user.getStatus())
                .kycStatus(user.getKycStatus())
                .createdAt(user.getCreatedAt())
                .provider(user.getProvider())
                .lastLoginAt(user.getLastLoginAt())
                .token(jwtToken)
                .refreshToken(refreshToken)
            .build();

        } catch (BadCredentialsException ex) {

            Users refreshedUser = loginAttemptService.increaseFailedAttemptAndLockIfNeeded(user.getId());

            if (refreshedUser.getLockedUntil() != null) {
                throw new LoginEmailOrUsernameException("Terlalu banyak percobaan gagal. Akun terkunci 5 menit");
            }

            throw new LoginEmailOrUsernameException( "Email/username atau password salah. Percobaan " + refreshedUser.getFailedAttempts() + "/5");
        }
    }

    public void saveLoginHistory(Users user, String ip, String userAgent, boolean success, String reason) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setIpAddress(ip);
        history.setUserAgent(userAgent);
        history.setLocation(null);
        history.setSuccess(success);
        history.setFailureReason(reason);
        history.setCreatedAt(LocalDateTime.now().withNano(0));

        loginHistoryRepository.save(history);

    }

}
