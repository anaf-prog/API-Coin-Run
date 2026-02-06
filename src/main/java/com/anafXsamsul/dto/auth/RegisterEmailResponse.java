package com.anafXsamsul.dto.auth;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterEmailResponse {
    private String info;
    private LocalDateTime otpSentAt;
    private LocalDateTime otpExpiredAt;

    @JsonIgnore
    private String otpToken;
}
