package com.anafXsamsul.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.UserStatus;

import jakarta.transaction.Transactional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {
    List<Users> findAllByRole(Users.UserRole role);

    @Query("""
        SELECT u FROM Users u
        WHERE u.email = :identifier
           OR u.username = :identifier
    """)
    Optional<Users> findByEmailOrUsername(@Param("identifier") String identifier);

    // Optional<Users> findByEmailOrUsername(@Param("identifier") String identifier);
    
    Optional<Users> findByEmail(String email);
    Optional<Users> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    Optional<Users> findByOtpToken(String otpToken);

    List<Users> findByStatusAndOtpExpiredAtBefore(UserStatus register, LocalDateTime now);

    @Modifying
    @Query(value = "UPDATE users SET failed_attempts = failed_attempts + 1 WHERE id = :userId", nativeQuery = true)
    @Transactional
    void incrementFailedAttempts(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE users SET failed_attempts = failed_attempts + 1, " + "locked_until = :lockedUntil WHERE id = :userId", nativeQuery = true)
    @Transactional
    void lockAccount(@Param("userId") Long userId,@Param("lockedUntil") LocalDateTime lockedUntil);

    @Modifying
    @Query(value = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE id = :userId", nativeQuery = true)
    @Transactional
    void resetFailedAttempts(@Param("userId") Long userId);
    
}
