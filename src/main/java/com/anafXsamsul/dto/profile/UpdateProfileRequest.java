package com.anafXsamsul.dto.profile;

import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String dateOfBirth;
    private String address;
    private String city;
    private String countryCode;
    private String postalCode;

    @Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Nomor telepon harus terdiri dari 10-15 digit angka dan boleh diawali tanda + untuk kode negara"
    )
    private String phoneNumber;
    
    private MultipartFile profileImage;
    
}
