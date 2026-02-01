package com.anafXsamsul.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.repository.UserRepository;

@Service
public class LoginAttemptService {

    @Autowired
    private UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Users increaseFailedAttemptAndLockIfNeeded(Long userId) {

        userRepository.incrementFailedAttempts(userId);

        Users user = userRepository.findById(userId).orElseThrow();

        if (user.getFailedAttempts() >= 5) {
            LocalDateTime lockedUntil = LocalDateTime.now().plusMinutes(5);
            userRepository.lockAccount(userId, lockedUntil);
            user = userRepository.findById(userId).orElseThrow();
        }

        return user;
    }
    
}
