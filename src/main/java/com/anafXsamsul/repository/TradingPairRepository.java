package com.anafXsamsul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.TradingPair;

@Repository
public interface TradingPairRepository extends JpaRepository<TradingPair, Long>, JpaSpecificationExecutor<TradingPair> {
    
}
