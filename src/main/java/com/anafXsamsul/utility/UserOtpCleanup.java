package com.anafXsamsul.utility;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.UserStatus;
import com.anafXsamsul.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class UserOtpCleanup {

    @Autowired
    private  UserRepository userRepository;

    @Scheduled(fixedRate = 60000)  
    @Transactional
    public void cleanupExpiredOtpUsers() {

        LocalDateTime now = LocalDateTime.now().withNano(0);

        List<Users> expired = userRepository.findByStatusAndOtpExpiredAtBefore(UserStatus.REGISTER, now);

        for (Users user : expired) {
            userRepository.delete(user);
            log.info("User berhasil dihapus karena OTP registrasi kadaluarsa : {}", user.getEmail());
        }
    }
}
