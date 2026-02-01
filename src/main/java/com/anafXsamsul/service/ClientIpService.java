package com.anafXsamsul.service;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ClientIpService {

    public String getClientIp (HttpServletRequest request) {

        // Daftar header bawa IP client
        String[] ipHeaders = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR",
            "CF-Connecting-IP"
        };

        // Cek semua header
        for (String header : ipHeaders) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // Ambil IP pertama jika ada multiple IP
                return ip.split(",")[0].trim();
            }
        }

        // Fallback ke default
        return request.getRemoteAddr();
    }
    
}
