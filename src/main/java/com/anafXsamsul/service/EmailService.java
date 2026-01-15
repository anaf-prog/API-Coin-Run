package com.anafXsamsul.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.UserRole;
import com.anafXsamsul.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendLoginNotification(String toEmail, String username, LocalDateTime loginTime) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Notifikasi Login Akun");
        message.setText(
                "Halo " + username + ",\n\n" +
                        "Kami mendeteksi login ke akun Anda.\n\n" +
                        "Waktu Login : " + loginTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")) + "\n\n"
                        +
                        "Jika ini bukan Anda, segera ganti password dan hubungi admin.\n\n" +
                        "Salam,\n" +
                        "Tim Keamanan");

        try {
            mailSender.send(message);
            logger.info("Email login berhasil dikirim ke {}", toEmail);
        } catch (Exception e) {
            logger.error("Gagal kirim email login ke {} : {}", toEmail, e.getMessage());
        }
    }

    public void sendOtpEmail(String toEmail, String username, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Kode OTP Registrasi");
        message.setText(
                "Halo " + username + ",\n\n" +
                        "Kode OTP registrasi Anda adalah:\n\n" +
                        otp + "\n\n" +
                        "OTP ini berlaku selama 5 menit.\n\n" +
                        "Jika Anda tidak merasa mendaftar, abaikan email ini.\n\n" +
                        "Salam,\nTim Security");

        mailSender.send(message);
    }

    public void sendPasswordResetNotification(String toEmail, String username) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Notifikasi Perubahan Password");
        message.setText("Halo " + username + ",\n\n"
                + "Password akun Anda telah berhasil diubah.\n"
                + "Jika Anda tidak melakukan perubahan ini, harap segera hubungi administrator.\n\n"
                + "Salam,\n"
                + "Tim Support");

        try {
            mailSender.send(message);
            logger.info("Email notifikasi password berhasil dikirim ke: " + toEmail);
        } catch (Exception e) {
            logger.error("Gagal mengirim email ke " + toEmail + ": " + e.getMessage());
        }
    }

    /**
     * Kirim email notifikasi ke SEMUA user dengan role USER
     * ketika ada reset password
     */
    @Transactional
    public void notifyAllUsersAboutPasswordReset(String changedUserEmail) {
        try {
            // Ambil semua user dengan role USER
            List<Users> allUsers = userRepository.findAllByRole(UserRole.USER);

            if (allUsers.isEmpty()) {
                logger.info("Tidak ada user dengan role USER untuk dikirimi notifikasi");
                return;
            }

            for (Users user : allUsers) {
                // Skip user yang sedang mereset password (optional)
                if (user.getEmail().equals(changedUserEmail)) {
                    continue;
                }

                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(fromEmail);
                message.setTo(user.getEmail());
                message.setSubject("Informasi Keamanan Sistem");
                message.setText("Halo " + user.getUsername() + ",\n\n"
                        + "Ada perubahan password pada akun user di sistem kami.\n"
                        + "Email yang diubah: " + changedUserEmail + "\n"
                        + "Waktu: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))
                        + "\n\n"
                        + "Ini adalah notifikasi otomatis untuk menjaga keamanan sistem.\n"
                        + "Jika Anda memiliki kekhawatiran, harap hubungi administrator.\n\n"
                        + "Salam,\n"
                        + "Tim Keamanan Sistem");

                mailSender.send(message);
                logger.info("Notifikasi berhasil dikirim ke: " + user.getEmail());
            }

            logger.info("Total " + allUsers.size() + " user telah dikirimi notifikasi");

        } catch (Exception e) {
            logger.error("Gagal mengirim notifikasi ke semua user: " + e.getMessage());
        }
    }

}
