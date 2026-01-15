package com.anafXsamsul.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.anafXsamsul.dto.PasswordResetSuccessEvent;
import com.anafXsamsul.service.EmailService;

@Component
public class PasswordResetEventListener {

    @Autowired
    private EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePasswordResetSuccess(PasswordResetSuccessEvent event) {

        emailService.sendPasswordResetNotification(event.getEmail(), event.getUsername(), event.getResetTime());

    }
    
}
