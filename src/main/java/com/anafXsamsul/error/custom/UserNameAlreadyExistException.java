package com.anafXsamsul.error.custom;

/**
 * Exception username custom untuk username yang sudah terdatar saat proses registrasi
 */
public class UserNameAlreadyExistException extends BusinessException {

    /**
     * Exception username custom dengan pesan custom.
     *
     * @param message pesan kesalahan
     */
    public UserNameAlreadyExistException(String message) {
        super(message);
    }

    /**
     * Exception username custom dengan pesan default.
     */
    public UserNameAlreadyExistException() {
        super("Username sudah terdaftar");
    }
    
}
