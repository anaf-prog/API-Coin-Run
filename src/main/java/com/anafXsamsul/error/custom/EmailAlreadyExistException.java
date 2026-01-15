package com.anafXsamsul.error.custom;

/**
 * Exception email custom untuk email yang sudah terdatar saat proses registrasi
 */
public class EmailAlreadyExistException extends BusinessException {

    /**
     * Exception email custom dengan pesan custom.
     *
     * @param message pesan kesalahan
     */
    public EmailAlreadyExistException(String message) {
        super(message);
    }

    /**
     * Exception email custom dengan pesan default.
     */
    public EmailAlreadyExistException() {
        super("Email sudah terdaftar");
    }
    
}
