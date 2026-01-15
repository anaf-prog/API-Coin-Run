package com.anafXsamsul.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Konfigurasi keamanan Spring Security untuk aplikasi. */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private Oauth2SuccessHandler oAuth2SuccessHandler;

    @Autowired
    private CustomOauth2UserService customOAuth2UserService;

    @Value("${security.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${security.cors.allowed-methods}")
    private String allowedMethods;

    @Value("${security.cors.allowed-headers}")
    private String allowedHeaders;

    @Value("${security.cors.allow-credentials}")
    private boolean allowCredentials;

    /** Mengatur filter chain Spring Security. */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/ws/**")
                .disable()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**", "/oauth2/**", "/login/oauth2/**",
                    "/api/public/**", "/ws/**",
                    "/", "/index.html", "/static/**", "/css/**", "/js/**", "/resources/**",
                    "/favicon.ico", "/favicon.*", "/error/**", "/h2-console/**"
                )
                .permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated()
            )

            // OAUTH2 LOGIN
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo ->
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2SuccessHandler)
            )

            // LOGOUT (Optional, sederhana saja)
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")  // Optional endpoint
                .logoutSuccessUrl("/")          // Redirect setelah logout
                .permitAll()
            )            
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /** Konfigurasi CORS untuk REST API. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of(allowedMethods.split(",")));
        configuration.setAllowedHeaders(List.of(allowedHeaders));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(allowCredentials);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /** Konfigurasi AuthenticationManager untuk login manual. */
    @Bean
    public AuthenticationManager authenticationManager(
        HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService
    ) throws Exception {

        AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);

        builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

        return builder.build();
    }

    /** Password encoder menggunakan BCrypt.*/
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}