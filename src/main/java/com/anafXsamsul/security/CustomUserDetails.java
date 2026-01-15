package com.anafXsamsul.security;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.anafXsamsul.entity.Users;

/**
 * Implementasi UserDetails milik Spring Security.
 *
 * <p>
 * Class ini berfungsi sebagai "jembatan" antara
 * entity {@link Users} dengan Spring Security.
 * </p>
 */
public class CustomUserDetails implements UserDetails {

    private final Users user;
    
    public CustomUserDetails(Users user) {
        this.user = user;
    }
    
    /**
     * Mengembalikan daftar role / authority user.
     *
     * <p>
     * Spring Security menggunakan authority ini
     * untuk proses authorization (cek akses endpoint).
     * </p>
     *
     * <p>
     * Format role wajib diawali dengan prefix "ROLE_".
     * Contoh: ROLE_USER, ROLE_ADMIN
     * </p>
     *
     * @return collection berisi role user
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }
    
    /** Mengembalikan password user. */
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    
    /**
     * Mengembalikan username yang digunakan untuk login.
     *
     * <p>
     * Dalam implementasi ini,
     * email dijadikan sebagai username utama.
     * </p>
     *
     * @return email user
     */
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    /** Menentukan apakah akun sudah kedaluwarsa atau belum. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    /** Menentukan apakah akun terkunci atau tidak. */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() != Users.UserStatus.SUSPENDED;
    }
    
    /** Menentukan apakah kredensial (password) sudah kedaluwarsa. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    /** Menentukan apakah akun aktif atau tidak. */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == Users.UserStatus.ACTIVE;
    }
    
    /**
     * Mengembalikan entity Users asli.
     *
     * <p>
     * Method ini berguna jika kita ingin
     * mengakses data user lengkap
     * dari SecurityContext.
     * </p>
     *
     * @return entity Users
     */
    public Users getUser() {
        return user;
    }
    
}
