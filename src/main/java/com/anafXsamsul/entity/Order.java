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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_id", nullable = false)
    private TradingPair pair;
    
    @Column(name = "order_id", unique = true, length = 50)
    private String orderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "side", length = 10)
    private OrderSide side;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20)
    private OrderType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private OrderStatus status;
    
    @Column(name = "price", precision = 36, scale = 18)
    private BigDecimal price;
    
    @Column(name = "quantity", precision = 36, scale = 18)
    private BigDecimal quantity;
    
    @Column(name = "filled_quantity", precision = 36, scale = 18)
    private BigDecimal filledQuantity = BigDecimal.ZERO;
    
    @Column(name = "fee", precision = 36, scale = 18)
    private BigDecimal fee;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @PrePersist
    public void prePersist() {
        if (orderId == null) {
            orderId = "ORD-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    public enum OrderSide {
        BUY, SELL
    }
    
    public enum OrderType {
        MARKET, LIMIT, STOP_LIMIT
    }
    
    public enum OrderStatus {
        OPEN, PARTIALLY_FILLED, FILLED, CANCELLED, EXPIRED, REJECTED
    }
}
