package com.anafXsamsul.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crypto_id", nullable = false)
    private Cryptocurrency cryptocurrency;
    
    @Column(name = "address", length = 255)
    private String address;
    
    @Column(name = "balance", precision = 36, scale = 18)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @Column(name = "locked_balance", precision = 36, scale = 18)
    private BigDecimal lockedBalance = BigDecimal.ZERO;
    
    @Column(name = "total_deposited", precision = 36, scale = 18)
    private BigDecimal totalDeposited = BigDecimal.ZERO;
    
    @Column(name = "total_withdrawn", precision = 36, scale = 18)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;
    
    @Column(name = "private_key_encrypted", length = 500)
    private String privateKeyEncrypted;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
}
