package com.anafXsamsul.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.PasswordResetToken;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long>, JpaSpecificationExecutor<PasswordResetToken> {
    Optional<PasswordResetToken> findByToken(String token);
    
}
