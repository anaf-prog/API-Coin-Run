package com.anafXsamsul.error.custom;

/**
 * Exception image custom untuk image / gmabar yang tidak valid
 */
public class ImageException extends BusinessException {

    /**
     * Exception image custom dengan pesan custom.
     *
     * @param message pesan kesalahan
     */
    public ImageException(String message) {
        super(message);
    }

    /**
     * Exception image custom dengan pesan default.
     */
    public ImageException() {
        super("Gambar tidak valid");
    }
    
}
