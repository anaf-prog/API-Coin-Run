package com.anafXsamsul.entity;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cryptocurrencies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cryptocurrency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "symbol", unique = true, nullable = false, length = 20)
    private String symbol;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;
    
    @Column(name = "network", length = 50)
    private String network;
    
    @Column(name = "decimals")
    private Integer decimals = 18;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "min_deposit_amount", precision = 36, scale = 18)
    private BigDecimal minDepositAmount;
    
    @Column(name = "min_withdrawal_amount", precision = 36, scale = 18)
    private BigDecimal minWithdrawalAmount;
    
    @Column(name = "withdrawal_fee", precision = 36, scale = 18)
    private BigDecimal withdrawalFee;
    
    // ============ RELATIONS ============
    @OneToMany(mappedBy = "cryptocurrency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Wallet> wallets = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "baseCryptocurrency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<TradingPair> basePairs = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "quoteCryptocurrency", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<TradingPair> quotePairs = new java.util.ArrayList<>();
    
}
