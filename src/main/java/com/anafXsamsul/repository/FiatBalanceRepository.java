package com.anafXsamsul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.FiatBalance;

@Repository
public interface FiatBalanceRepository extends JpaRepository<FiatBalance, Long>, JpaSpecificationExecutor<FiatBalance> {
    
}
