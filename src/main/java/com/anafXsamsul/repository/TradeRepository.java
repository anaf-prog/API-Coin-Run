package com.anafXsamsul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.Trade;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long>, JpaSpecificationExecutor<Trade> {
    
}
