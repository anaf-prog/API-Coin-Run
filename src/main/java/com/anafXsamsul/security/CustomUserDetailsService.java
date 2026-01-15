package com.anafXsamsul.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.anafXsamsul.entity.Users;
import com.anafXsamsul.repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Service untuk mengambil data user dari database
 * berdasarkan email atau username.
 *
 * <p>
 * Class ini merupakan implementasi dari {@link UserDetailsService}
 * yang digunakan oleh Spring Security saat proses login manual
 * (email/username + password).
 * </p>
 *
 * <p>
 * Spring Security akan memanggil method
 * {@link #loadUserByUsername(String)} secara otomatis
 * ketika user mencoba login.
 * </p>
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Mengambil data user berdasarkan identifier.
     *
     * @param identifier email atau username yang dimasukkan user saat login
     * @return UserDetails yang dibutuhkan oleh Spring Security
     * @throws UsernameNotFoundException jika user tidak ditemukan
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // Cari user by email atau username
        Users user = userRepository.findByEmailOrUsername(identifier)
            .orElseThrow(() -> 
                new UsernameNotFoundException("User not found with identifier: " + identifier));
        
        return new CustomUserDetails(user);
    }
}
