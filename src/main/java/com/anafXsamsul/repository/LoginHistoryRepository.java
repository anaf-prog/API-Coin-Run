package com.anafXsamsul.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.LoginHistory;

@Repository
public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long>, JpaSpecificationExecutor<LoginHistory> {
    
}
