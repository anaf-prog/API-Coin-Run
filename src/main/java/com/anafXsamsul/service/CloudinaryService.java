package com.anafXsamsul.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.anafXsamsul.error.custom.ImageException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    /**
     * Upload image ke Cloudinary
     *
     * @param file   MultipartFile dari request
     * @param folder folder di cloudinary (misal: profile)
     * @return secure_url dari cloudinary
     */
    public Map<String, String> uplodaImage(MultipartFile file, String folder) {
        try {
            Map<String, Object> options = new HashMap<>();
            options.put("folder", folder);
            options.put("resource_type", "image");

            @SuppressWarnings("rawtypes")
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            Map<String, String> result = new HashMap<>();
            result.put("url", uploadResult.get("secure_url").toString());
            result.put("publicId", uploadResult.get("public_id").toString());

            return result;

        } catch (IOException e) {
            log.error("Gagal upload gambar ke cloudinary : {} ", e.getMessage());
            throw new ImageException("Gagal upload gambar");
        }
    }
    
    /**
     * Delete image dari Cloudinary
     */
    public void deleteImage(String publicId) {
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Gagal hapus gambar di cloudinary : {} ", e.getMessage());
        }
    }
}
