package com.anafXsamsul.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import com.anafXsamsul.dto.profile.UpdateProfileRequest;
import com.anafXsamsul.dto.profile.UserProfileResponse;
import com.anafXsamsul.entity.UserProfile;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.error.custom.BusinessException;
import com.anafXsamsul.error.custom.ImageException;
import com.anafXsamsul.repository.UserProfileRepository;
import com.anafXsamsul.repository.UserRepository;
import com.anafXsamsul.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    public UserProfileResponse getMyProfile() {

        Users user = getCurrentUser();

        return mapToUserProfileResponse(user.getProfile());
    }

    @Transactional
    public UserProfileResponse updateProfileUser(UpdateProfileRequest request) {
        
        // Find user
        Users user = getCurrentUser();
        
        // Get or create user profile
        UserProfile userProfile = user.getProfile();
        if (userProfile == null) {
            userProfile = new UserProfile();
            userProfile.setUser(user);
            user.setProfile(userProfile);
        }
        
        // Update profile fields
        userProfile.setFirstName(request.getFirstName());
        userProfile.setLastName(request.getLastName());

        if (request.getDateOfBirth() != null) {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                LocalDate dob = LocalDate.parse(request.getDateOfBirth(), formatter);
                userProfile.setDateOfBirth(dob);
            } catch (DateTimeParseException e) {
                throw new BusinessException("Format tanggal lahir harus dd-MM-yyyy");
            }
        }

        userProfile.setAddress(request.getAddress());
        userProfile.setCity(request.getCity());
        userProfile.setCountryCode(request.getCountryCode());
        userProfile.setPostalCode(request.getPostalCode());
        userProfile.setUpdatedAt(LocalDateTime.now().withNano(0));

        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()) {
            try {

                String oldPublic = userProfile.getProfileImageId();

                Map<String, String> uploadResult = cloudinaryService.uplodaImage(request.getProfileImage(), "profile");

                userProfile.setProfileImageUrl(uploadResult.get("url"));
                userProfile.setProfileImageId(uploadResult.get("publicId"));

                if (oldPublic != null) {
                    cloudinaryService.deleteImage(oldPublic);
                }
                
            } catch (Exception e) {
                log.error("Gagal upload foto profile : {} ", e.getMessage());
                throw new ImageException("Gagal upload foto");
            }
        }
        
        // Save profile
        userProfileRepository.save(userProfile);
        userRepository.save(user);

        log.info("Profile user dengan ID {} berhasil di update", user.getId());
        
        return mapToUserProfileResponse(userProfile);
    }

    private Users getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }

    private UserProfileResponse mapToUserProfileResponse(UserProfile profile) {
        Users user = profile.getUser();

        return UserProfileResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .phoneNumber(user.getPhoneNumber())
            .firstName(profile.getFirstName())
            .lastName(profile.getLastName())
            .address(profile.getAddress())
            .city(profile.getCity())
            .dateOfBirth(profile.getDateOfBirth())
            .profileImageUrl(profile.getProfileImageUrl())
            .status(user.getStatus())
            .kycStatus(user.getKycStatus())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .lastLoginAt(user.getLastLoginAt())
        .build();
    }

}
