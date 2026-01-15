package com.anafXsamsul.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.anafXsamsul.entity.Users;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private String address;
    private String city;

    @JsonFormat(pattern = "dd-MM-yyyy")
    private LocalDate dateOfBirth;

    private String profileImageUrl;
    private Users.UserStatus status;
    private Users.KycStatus kycStatus;
    private Users.UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;
}
