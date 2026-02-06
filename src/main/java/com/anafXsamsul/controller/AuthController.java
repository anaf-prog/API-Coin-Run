package com.anafXsamsul.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.dto.auth.AuthResponse;
import com.anafXsamsul.dto.auth.LoginRequest;
import com.anafXsamsul.dto.auth.LoginResponse;
import com.anafXsamsul.dto.auth.RegisterEmailRequest;
import com.anafXsamsul.dto.auth.RegisterEmailResponse;
import com.anafXsamsul.dto.auth.RegisterRequest;
import com.anafXsamsul.dto.auth.ResendOtpRequest;
import com.anafXsamsul.dto.auth.ResendOtpResponse;
import com.anafXsamsul.dto.auth.VerifyOtpRequest;
import com.anafXsamsul.service.AuthService;
import com.anafXsamsul.service.ClientIpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private ClientIpService clientIpService;

    @PostMapping("/register-email")
    public ResponseEntity<ApiResponse<RegisterEmailResponse>> registerEmail( @Valid @RequestBody RegisterEmailRequest request, HttpServletResponse httpResponse, HttpServletRequest httpRequest) {
        RegisterEmailResponse response = authService.registerEmail(request);

        String ip = (String) httpRequest.getAttribute("clientIp");
        String ua = (String) httpRequest.getAttribute("userAgent");

        log.info("Ip user yang melakukan registrasi email : " + ip);
        log.info("User Agent yang melakukan registrasi email : " + ua);

        ResponseCookie cookie = ResponseCookie.from("OTP_TOKEN", response.getOtpToken())
            .httpOnly(true)
            .secure(false)
            .path("/")
            .maxAge(Duration.ofMinutes(5))
            .sameSite("Strict")
        .build();

        // settingan cookie di server
        // ResponseCookie cookie = ResponseCookie.from("OTP_TOKEN", otpToken)
        // .httpOnly(true)
        // .secure(true)
        // .path("/")
        // .sameSite("None")
        // .build();

    httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        ApiResponse<RegisterEmailResponse> apiResponse = ApiResponse.<RegisterEmailResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request, @CookieValue("OTP_TOKEN") String otpToken, HttpServletRequest httpServletRequest) {
        AuthResponse response = authService.register(request, otpToken);

        String ip = (String) httpServletRequest.getAttribute("clientIp");
        String ua = (String) httpServletRequest.getAttribute("userAgent");

        log.info("Ip user yang melakukan registrasi : " + ip);
        log.info("User Agent yang melakukan registrasi : " + ua);

        ApiResponse<AuthResponse> apiResponse = ApiResponse.<AuthResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.status(200).body(apiResponse);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@RequestBody VerifyOtpRequest request, @CookieValue("OTP_TOKEN") String otpToken, HttpServletRequest httpServletRequest) {
        AuthResponse response = authService.verifyOtp(request, otpToken);

        String ip = (String) httpServletRequest.getAttribute("clientIp");
        String ua = (String) httpServletRequest.getAttribute("userAgent");

        log.info("Ip user yang menerima otp : " + ip);
        log.info("User Agent yang menerima otp : " + ua);

        return ResponseEntity.ok(
            ApiResponse.<AuthResponse>builder()
                .statusCode(200)
                .message("success")
                .data(response)
            .build()
        );
    }


    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<ResendOtpResponse>> resendOtp(@RequestBody @Valid ResendOtpRequest request, HttpServletRequest httpServletRequest) {

        ResendOtpResponse response = authService.resendOtp(request.getEmail());

        String ip = (String) httpServletRequest.getAttribute("clientIp");
        String ua = (String) httpServletRequest.getAttribute("userAgent");

        log.info("Ip user yang melakukan request otp : " + ip);
        log.info("User Agent yang melakukan request otp  : " + ua);

        ApiResponse<ResendOtpResponse> apiResponse = ApiResponse.<ResendOtpResponse>builder()
            .statusCode(200)
            .message("success")
            .data(response)
        .build();

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {

        String ip = clientIpService.getClientIp(servletRequest);
        String userAgent = servletRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ip, userAgent);
        return ResponseEntity.ok(response);
    }

    // Frontend akan langsung mengarahkan user ke endpoint OAuth2 di backend
    @GetMapping("/oauth2/url/google")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String authUrl = "http://localhost:8082/oauth2/authorization/google";

        return ResponseEntity.ok(Map.of(
                "url", authUrl,
                "method", "GET"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {

        return ResponseEntity.ok(Map.of(
                "message", "Logout successful",
                "timestamp", LocalDateTime.now()));
    }
}
