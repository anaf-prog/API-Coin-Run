package com.anafXsamsul.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.anafXsamsul.dto.ApiResponse;
import com.anafXsamsul.dto.UpdateProfileRequest;
import com.anafXsamsul.dto.UserProfileResponse;
import com.anafXsamsul.service.ProfileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/profile")
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    // End point untuk mendapatkan profile user yang sedang login
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {

        UserProfileResponse profile = profileService.getMyProfile();

        return ResponseEntity.ok(
            ApiResponse.<UserProfileResponse>builder()
                .statusCode(200)
                .message("success")
                .data(profile)
            .build()
        );
    }

    // End point untuk edit profile user yang sedang login
    @PatchMapping(value =  "/user-update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(@ModelAttribute @Valid UpdateProfileRequest request) {

        UserProfileResponse profile = profileService.updateProfileUser(request);

        return ResponseEntity.ok(
            ApiResponse.<UserProfileResponse>builder()
                .statusCode(200)
                .message("success")
                .data(profile)
            .build()
        );
    }
    
}
