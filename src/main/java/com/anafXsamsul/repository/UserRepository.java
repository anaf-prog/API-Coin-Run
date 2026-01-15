package com.anafXsamsul.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anafXsamsul.entity.Users;

@Repository
public interface UserRepository extends JpaRepository<Users, Long>, JpaSpecificationExecutor<Users> {

    @Query("SELECT u FROM Users u WHERE u.email = :identifier OR u.username = :identifier")

    Optional<Users> findById(Long userId);

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
    List<Users> findByStatus(Users.UserStatus status);
    List<Users> findByRole(Users.UserRole role);
    
    @Query("SELECT u FROM Users u WHERE u.lastLoginAt < :date AND u.status = 'ACTIVE'")
    List<Users> findInactiveUsers(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(u) FROM Users u WHERE u.createdAt >= :startDate AND u.createdAt < :endDate")
    Long countNewUsers(@Param("startDate") LocalDateTime startDate, 
                       @Param("endDate") LocalDateTime endDate);
    
}
