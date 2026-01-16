package com.anafXsamsul.error.custom;

/**
 * Exception untuk menandai terjadinya kesalahan pada aturan bisnis.
 */
public class BusinessException extends RuntimeException {

    /**
     * Membuat BusinessException dengan pesan error.
     *
     * @param message pesan kesalahan bisnis
     */
    public BusinessException(String message) {
        super(message);
    }

    /**
     * Exception bussiness custom dengan pesan default.
     */
    public BusinessException() {
        super("Other Error");
    }
    
}
