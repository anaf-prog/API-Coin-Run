package com.anafXsamsul.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.anafXsamsul.PrettyPrintResultHandler;
import com.anafXsamsul.dto.RegisterRequest;
import com.anafXsamsul.dto.VerifyOtpRequest;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private EmailService emailService;

    @BeforeEach
    public void setup(){
        userRepository.deleteAll();
    }
    
    // Test register success
    @Test
    void testRegisterSuccess() throws Exception {
        // Persiapan data
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@gmail.com");
        request.setUsername("userbaru");
        request.setPassword("Rahasia123@.");
        request.setPhoneNumber("089606891270");

        // Mocking email service agar tidak mengirim beneran
        doNothing().when(emailService).sendOtpEmail(anyString(), anyString(), anyString());

        // Eksekusi
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("success"));

        boolean userExists = userRepository.existsByEmail("user@gmail.com");
        System.out.println(">>> Verifikasi DB: Apakah user masuk? " + userExists);

        Optional<Users> userDb = userRepository.findByUsername(request.getUsername());
        System.out.println(">>> Verifikasi User dari DB : " + userDb);
        System.out.println(">>> Isi OTP User dari DB : " + userDb.get().getOtpCode());

        Assertions.assertTrue(  userExists, "User ada didatabase setelah registrasi" );
    }

    // Test verifikasi OTP success
    @Test
    void testVerifyOtpSuccess() throws Exception {
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

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("verify@gmail.com");
        request.setOtp("123456");

        mockMvc.perform(post("/api/auth/verify-otp")
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

    // Test register gagal karena email tidak valid
    @Test
    void testRegisterEmailTidakValid() throws Exception {

        // Persiapan data
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@test.com"); // Email tidak valid
        request.setUsername("userbaru");
        request.setPassword("Rahasia123@.");
        request.setPhoneNumber("089606891270");

        // Eksekusi API
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            // Verifikasi ekspektasi: Status harus 400 (Bad Request)
            .andExpect(status().isBadRequest())
            // Pastikan pesan error sesuai dengan yang dilempar EmailAlreadyExistException
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        System.out.println(">>> Verifikasi: Registrasi dengan email tidak valid sesuai ekspektasi");

    }

    // Test register gagal karena email sudah terdaftar
    @Test
    void testRegisterEmailAlreadyExists() throws Exception {
        // Simpan data mock ke database
        Users existingUser = new Users();
        existingUser.setEmail("user@gmail.com");
        existingUser.setUsername("userlama");
        existingUser.setPassword("Rahasia123@.");
        existingUser.setPhoneNumber("089606891270");
        userRepository.save(existingUser);

        // Persiapan data
        RegisterRequest request = new RegisterRequest();
        request.setEmail("user@gmail.com");
        request.setUsername("userbaru");
        request.setPassword("Rahasia123@.");
        request.setPhoneNumber("089606891270");

        // Eksekusi API
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            // Verifikasi ekspektasi: Status harus 400 (Bad Request)
            .andExpect(status().isBadRequest())
            // Pastikan pesan error sesuai dengan yang dilempar EmailAlreadyExistException
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        System.out.println(">>> Verifikasi: Registrasi dengan email sudah terdaftar gagal sesuai ekspektasi");
        Optional<Users> userDb = userRepository.findByEmail(request.getEmail());
        System.out.println(">>> Verifikasi User dari DB sudah terdaftar : " + userDb);

    }

    // Test register gagal karena username sudah terdaftar
    @Test
    void testRegisterUsernameAlreadyExist() throws Exception {
        // Simpan data mock ke database
        Users existingUser = new Users();
        existingUser.setEmail("user@gmail.com");
        existingUser.setUsername("userlama");
        existingUser.setPassword("Rahasia123@.");
        existingUser.setPhoneNumber("089606891270");
        userRepository.save(existingUser);

        // Persiapan data
        RegisterRequest request = new RegisterRequest();
        request.setEmail("userbaru@gmail.com");
        request.setUsername("userlama");
        request.setPassword("Rahasia123@.");
        request.setPhoneNumber("089606891270");

        // Eksekusi API
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            // Verifikasi ekspektasi: Status harus 400 (Bad Request)
            .andExpect(status().isBadRequest())
            // Pastikan pesan error sesuai dengan yang dilempar UsernameAlreadyExistException
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        System.out.println(">>> Verifikasi: Registrasi dengan username sudah terdaftar gagal sesuai ekspektasi");
        Optional<Users> userDb = userRepository.findByUsername(request.getUsername());
        System.out.println(">>> Verifikasi User dari DB sudah terdaftar : " + userDb);
    }

    // Test register gagal karena password tidak sesuai aturan
    @Test
    void testRegisterPasswordTidakSesuai() throws Exception {

        // Persiapan data
        RegisterRequest request = new RegisterRequest();
        request.setEmail("userbaru@gmail.com");
        request.setUsername("userlama");
        request.setPassword("rahasia123"); // Password tanpa karakter khusus dan tanapa huruf besar
        request.setPhoneNumber("089606891270");

        // Eksekusi API
        mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            // Verifikasi ekspektasi: Status harus 400 (Bad Request)
            .andExpect(status().isBadRequest())
            // Pastikan pesan error sesuai dengan yang dilempar
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        System.out.println(">>> Verifikasi: Registrasi gagal karena password tidak sesuai aturan");
    }

    // Test verifikasi OTP gagal karena OTP salah
    @Test
    void testVerifyOtpGagal() throws Exception {
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

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail("verify@gmail.com");
        request.setOtp("123455");

        mockMvc.perform(post("/api/auth/verify-otp")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andDo(PrettyPrintResultHandler.printBodyOnly())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.statusCode").value(400))
        .andExpect(jsonPath("$.message").isNotEmpty());

        Optional<Users> userDb = userRepository.findByEmail(request.getEmail());
        System.out.println(">>> Verifikasi User dari DB : " + userDb);
        System.out.println(">>> Isi OTP User dari DB : " + userDb.get().getOtpCode());
    }

}
    