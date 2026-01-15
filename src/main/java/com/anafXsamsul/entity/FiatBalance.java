package com.anafXsamsul.entity;

import java.math.BigDecimal;

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
@Table(name = "fiat_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FiatBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @Column(name = "currency_code", length = 10, nullable = false)
    private String currencyCode;
    
    @Column(name = "available_balance", precision = 20, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO;
    
    @Column(name = "locked_balance", precision = 20, scale = 2)
    private BigDecimal lockedBalance = BigDecimal.ZERO;
    
}
