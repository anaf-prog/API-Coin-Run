package com.anafXsamsul.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.anafXsamsul.PrettyPrintResultHandler;
import com.anafXsamsul.dto.resetpassword.ForgotPasswordRequest;
import com.anafXsamsul.dto.resetpassword.ResetPasswordRequest;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ResetPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    public void setup() {
        userRepository.deleteAll();
    }

    // Test forgot password
    @Test
    void testForgotPasswordSuccess() throws Exception {
        // Buat data user dengan OTP untuk testing
        Users user = new Users();
        user.setEmail("verify@gmail.com");
        user.setUsername("verifyuser");
        user.setPassword("Password123@.");
        user.setPhoneNumber("089606891271");
        user.setCreatedAt(LocalDateTime.now().withNano(0));
        user.setStatus(Users.UserStatus.PENDING);
        user.setOtpCode("123456");
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));
        user.setEmailVerified(false);
        userRepository.save(user);

        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setEmail("verify@gmail.com");

        // Mocking email service agar tidak mengirim beneran
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("success"));

        Optional<Users> userDb = userRepository.findByEmail(request.getEmail());
        System.out.println(">>> Verifikasi User dari DB : " + userDb);
        System.out.println(">>> Isi OTP User dari DB : " + userDb.get().getOtpCode());
    }

    // Test reset password
    @Test
    void testResetPasswordSuccess() throws Exception {
        // Buat data user dengan OTP untuk testing
        Users user = new Users();
        user.setEmail("verify@gmail.com");
        user.setUsername("verifyuser");
        user.setPassword("Password123@.");
        user.setPhoneNumber("089606891271");
        user.setCreatedAt(LocalDateTime.now().withNano(0));
        user.setStatus(Users.UserStatus.ACTIVE);
        user.setOtpCode("123456");
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));
        user.setEmailVerified(false);
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("verify@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("Password123@.!");
        request.setConfirmNewPassword("Password123@.!");

        // Mocking email service agar tidak mengirim beneran
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("success"));

        Optional<Users> userDb = userRepository.findByEmail(request.getEmail());
        System.out.println(">>> Verifikasi User dari DB : " + userDb);
        System.out.println(">>> Isi OTP User dari DB : " + userDb.get().getOtpCode());
    }

    // Test gagal reset password
    @Test
    void testResetPasswordGagal() throws Exception {
        // Buat data user dengan OTP untuk testing
        Users user = new Users();
        user.setEmail("verify@gmail.com");
        user.setUsername("verifyuser");
        user.setPassword("Password123@.");
        user.setPhoneNumber("089606891271");
        user.setCreatedAt(LocalDateTime.now().withNano(0));
        user.setStatus(Users.UserStatus.ACTIVE);
        user.setOtpCode("123456");
        user.setOtpExpiredAt(LocalDateTime.now().plusMinutes(5));
        user.setEmailVerified(false);
        userRepository.save(user);

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setEmail("verify@gmail.com");
        request.setOtp("123456");
        request.setNewPassword("Password123@.!");
        request.setConfirmNewPassword("Password123@"); // konfirmasi password tidak sama dengan new password

        // Mocking email service agar tidak mengirim beneran
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            // Verifikasi ekspektasi: Status harus 400 (Bad Request)
            .andExpect(status().isBadRequest())
            // Pastikan pesan error sesuai dengan yang dilempar
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        Optional<Users> userDb = userRepository.findByEmail(request.getEmail());
        System.out.println(">>> Verifikasi User dari DB : " + userDb);
        System.out.println(">>> Isi OTP User dari DB : " + userDb.get().getOtpCode());
    }
    
}
