package com.anafXsamsul.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_keys")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @Column(name = "api_key", unique = true, length = 100)
    private String apiKey;
    
    @Column(name = "api_secret_encrypted", length = 500)
    private String apiSecretEncrypted;
    
    @Column(name = "name", length = 100)
    private String name;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (apiKey == null) {
            apiKey = "API-" + java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
