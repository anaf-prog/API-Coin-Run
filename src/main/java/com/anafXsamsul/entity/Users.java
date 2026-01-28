package com.anafXsamsul.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_status", columnList = "status"),
        @Index(name = "idx_users_provider", columnList = "provider"),
        @Index(name = "idx_users_provider_id", columnList = "provider_id"),
        @Index(name = "idx_users_otp_token", columnList = "otp_token")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@ToString(exclude = "profile")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'USER'")
    private UserRole role = UserRole.USER;

    @Column(name = "username", unique = true, length = 100)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    // ============ SOCIAL LOGIN FIELDS (TAMBAHAN BARU) ============
    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // "google", "facebook", "local"

    @Column(name = "provider_id", length = 255)
    private String providerId; // ID dari provider (Google/Facebook)

    @Column(name = "oauth2_email", length = 255)
    private String oauth2Email; // Email dari OAuth2 provider

    // Password diubah menjadi nullable untuk support social login
    @Column(name = "password", nullable = true, length = 255)
    private String password;
    // ============ END SOCIAL LOGIN FIELDS ============

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'ACTIVE'")
    private UserStatus status = UserStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'NOT_SUBMITTED'")
    private KycStatus kycStatus = KycStatus.NOT_SUBMITTED;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_token", length = 255, unique = true)
    private String otpToken;

    @Column(name = "otp_expired_at")
    private LocalDateTime otpExpiredAt;

    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    // ============ RELATIONS ============
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private UserProfile profile;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Wallet> wallets = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FiatBalance> fiatBalances = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiKey> apiKeys = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LoginHistory> loginHistories = new ArrayList<>();

    // ============ ENUMS ============
    public enum UserStatus {
        ACTIVE, SUSPENDED, CLOSED, PENDING, REGISTER
    }

    public enum KycStatus {
        NOT_SUBMITTED, PENDING, VERIFIED, REJECTED
    }

    public enum UserRole {
        ADMIN, USER
    }

    public enum AuthProvider {
        LOCAL, GOOGLE, FACEBOOK, APPLE, GITHUB, GITLAB
    }

    // ============ HELPER METHODS (TAMBAHAN BARU) ============

    /** Mengecek apakah user memiliki role ADMIN. */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }

    /** Mengecek apakah user adalah social login user (Google/Facebook) */
    public boolean isSocialUser() {
        return provider != null && provider != AuthProvider.LOCAL;
    }

    /** Mengecek apakah user adalah Google user */
    public boolean isGoogleUser() {
        return provider == AuthProvider.GOOGLE;
    }

    /** Mengecek apakah user adalah local user (registrasi manual) */
    public boolean isLocalUser() {
        return provider == null || provider == AuthProvider.LOCAL;
    }

    /** Mengecek apakah user saat ini berstatus ACTIVE. */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    /** Mengecek apakah status KYC user sudah VERIFIED. */
    public boolean isKycVerified() {
        return kycStatus == KycStatus.VERIFIED;
    }

    /**
     * Mengecek apakah user memiliki role tertentu.
     *
     * @param requiredRole role yang harus dimiliki user
     * @return true jika role user sama dengan requiredRole, false jika tidak
     */
    public boolean hasRole(UserRole requiredRole) {
        return this.role == requiredRole;
    }

    /**
     * Mengecek apakah user memiliki SALAH SATU dari role yang diberikan.
     * <pre>
     * Contoh pemakaian:
     * user.hasAnyRole(UserRole.ADMIN);
     * user.hasAnyRole(UserRole.ADMIN, UserRole.USER);
     * </pre>
     *
     * @param roles satu atau lebih role yang diperbolehkan
     * @return true jika role user cocok dengan salah satu role yang diberikan,
     *         false jika tidak ada yang cocok
     */
    public boolean hasAnyRole(UserRole... roles) {
        for (UserRole requiredRole : roles) {
            if (this.role == requiredRole) {
                return true;
            }
        }
        return false;
    }


}