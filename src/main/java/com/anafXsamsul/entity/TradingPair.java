package com.anafXsamsul.entity;

import java.math.BigDecimal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trading_pairs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradingPair {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "symbol", unique = true, nullable = false, length = 20)
    private String symbol; // Contoh: BTC/USD, ETH/BTC
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_crypto_id", nullable = false)
    private Cryptocurrency baseCryptocurrency;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_crypto_id", nullable = false)
    private Cryptocurrency quoteCryptocurrency;
    
    @Column(name = "price_precision")
    private Integer pricePrecision = 8;
    
    @Column(name = "quantity_precision")
    private Integer quantityPrecision = 8;
    
    @Column(name = "min_trade_amount", precision = 36, scale = 18)
    private BigDecimal minTradeAmount;
    
    @Column(name = "max_trade_amount", precision = 36, scale = 18)
    private BigDecimal maxTradeAmount;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    // ============ RELATIONS ============
    @OneToMany(mappedBy = "pair", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Order> orders = new java.util.ArrayList<>();
    
    @OneToMany(mappedBy = "pair", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<Trade> trades = new java.util.ArrayList<>();
    
}
