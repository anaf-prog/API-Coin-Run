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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "trades")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false)
    private TradingPair pair;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_order_id")
    private Order buyOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sell_order_id")
    private Order sellOrder;
    
    @Column(name = "trade_id", unique = true, length = 50)
    private String tradeId;
    
    @Column(name = "price", precision = 36, scale = 18)
    private BigDecimal price;
    
    @Column(name = "quantity", precision = 36, scale = 18)
    private BigDecimal quantity;
    
    @Column(name = "taker_side", length = 10)
    private String takerSide; // "BUY" or "SELL"
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    public void prePersist() {
        if (tradeId == null) {
            tradeId = "TRD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
}
