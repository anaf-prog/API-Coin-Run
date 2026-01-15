package com.anafXsamsul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.Cryptocurrency;

@Repository
public interface CryptocurrencyRepository extends JpaRepository<Cryptocurrency, Long>, JpaSpecificationExecutor<Cryptocurrency> {
    
}
