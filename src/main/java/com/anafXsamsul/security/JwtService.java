package com.anafXsamsul.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Service untuk mengelola JSON Web Token (JWT).
 * Token yang dibuat menggunakan algoritma HS256
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    /** Mengambil username/email dari JWT. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Mengambil claim tertentu dari JWT menggunakan resolver. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /** Membuat access token standar untuk user. */
    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    /** Membuat access token dengan tambahan custom claims. */
    public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpiration);
    }

    /** Membuat refresh token untuk user. */
    public String generateRefreshToken(UserDetails userDetails) {
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    /** Method inti untuk membangun JWT. */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts
            .builder()
            .setClaims(extraClaims)
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getSignInKey(), SignatureAlgorithm.HS256)
        .compact();
    }

    /** Memvalidasi JWT berdasarkan user dan waktu kedaluwarsa. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /** Mengecek apakah JWT sudah kedaluwarsa. */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Mengambil waktu kedaluwarsa dari JWT. */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /** Mengambil seluruh claims dari JWT. */
    private Claims extractAllClaims(String token) {
        return Jwts
            .parserBuilder()
            .setSigningKey(getSignInKey())
            .build()
            .parseClaimsJws(token)
        .getBody();
    }

    /** Membuat key untuk signing JWT dari secret key. */
    private Key getSignInKey() {
        return Keys.hmacShaKeyFor(
            secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }
}
