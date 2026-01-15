package com.anafXsamsul.error.custom;

/**
 * Exception login custom untuk pengecekan username atau email yang terdatar saat proses login
 */
public class LoginEmailOrUsernameException extends BusinessException {

    /**
     * Exception login custom dengan pesan custom.
     *
     * @param message pesan kesalahan
     */
    public LoginEmailOrUsernameException(String message) {
        super(message);
    }

    /**
     * Exception login custom dengan pesan default.
     */
    public LoginEmailOrUsernameException() {
        super("Email atau Username salah");
    }
    
}
