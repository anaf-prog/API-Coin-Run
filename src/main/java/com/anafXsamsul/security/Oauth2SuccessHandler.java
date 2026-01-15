package com.anafXsamsul.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/** Handler yang dijalankan setelah login OAuth2 berhasil. */
@Slf4j
@Component
public class Oauth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.oauth2.redirect-url}")
    private String redirectUrl;

    /**
     * Method yang dijalankan saat login OAuth2 berhasil.
     */
    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        log.debug("[Oauth2] Authentication sukses trigger");

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauthUser = oauthToken.getPrincipal();

        log.debug("[Oauth2] Attribut dari provider : {}", oauthUser.getAttributes());

        String email = oauthUser.getAttribute("email");

        // User SUDAH dipastikan valid & tersimpan oleh CustomOauth2UserService
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new LoginEmailOrUsernameException("Email tidak terdaftar"));

        CustomUserDetails userDetails = new CustomUserDetails(user);

        // Generate JWT token
        String token = jwtService.generateToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        log.debug("[Oauth2] proses generate JWT dengan user : {}", user.getUsername());

        // Build redirect URL dengan token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUrl)
            .queryParam("token", token)
            .queryParam("refreshToken", refreshToken)
            .queryParam("userId", user.getId())
            .queryParam("email", user.getEmail())
            .queryParam("username", user.getUsername())
            .queryParam("role", user.getRole().name())
            .build()
        .toUriString();

        log.debug("[Oauth2] redirect user ke url : {}", targetUrl);

        // Redirect ke frontend / backend callback
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

}
