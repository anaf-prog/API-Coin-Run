package com.anafXsamsul.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50)
    private TransactionType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private TransactionStatus status;
    
    @Column(name = "currency", length = 10)
    private String currency;
    
    @Column(name = "amount", precision = 36, scale = 18)
    private BigDecimal amount;
    
    @Column(name = "fee", precision = 36, scale = 18)
    private BigDecimal fee;
    
    @Column(name = "from_address", length = 255)
    private String fromAddress;
    
    @Column(name = "to_address", length = 255)
    private String toAddress;
    
    @Column(name = "description", length = 500)
    private String description;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;
    
    @Column(name = "required_confirmations")
    private Integer requiredConfirmations = 6;
    
    @Column(name = "confirmed_blocks")
    private Integer confirmedBlocks;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    public enum TransactionType {
        DEPOSIT, WITHDRAWAL, TRANSFER, SWAP, TRADE, EARN, STAKING, AIRDROP
    }
    
    public enum TransactionStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, CONFIRMING
    }
    
}
