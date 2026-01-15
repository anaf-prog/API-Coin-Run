package com.anafXsamsul.security;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import com.anafXsamsul.entity.UserProfile;
import com.anafXsamsul.entity.Users;
import com.anafXsamsul.entity.Users.AuthProvider;
import com.anafXsamsul.error.custom.LoginEmailOrUsernameException;
import com.anafXsamsul.repository.UserProfileRepository;
import com.anafXsamsul.repository.UserRepository;
import jakarta.transaction.Transactional;

/**
 * Service custom untuk menangani proses login OAuth2 (Google).
 * Class ini akan otomatis dipanggil oleh Spring Security
 * saat proses login Google berhasil.
 */
@Service
@Transactional
public class CustomOauth2UserService extends DefaultOAuth2UserService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    /**
     * Method utama yang dipanggil Spring Security
     * ketika user berhasil login menggunakan OAuth2 (Google).
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. Load user dari Google
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        // 2. Proses data user dari Google
        return processOAuth2User(userRequest, oAuth2User);
    }

    /** Memproses data user yang didapat dari provider OAuth2. */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        AuthProvider provider = AuthProvider.valueOf(userRequest.getClientRegistration().getRegistrationId().toUpperCase());

        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        // 3. Extract data dari Google
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");
        
        if (email == null) {
            throw new LoginEmailOrUsernameException("Email tidak valid");
        }
        
        // 4. Cari atau buat user baru
        Optional<Users> userOptional = userRepository.findByEmail(email);
        Users user;
        
        if (userOptional.isPresent()) {
            // User sudah ada - update info
            user = userOptional.get();
            
            // Update provider info jika belum ada
            if (user.getProvider() == null || !user.getProvider().equals(provider)) {
                user.setProvider(provider);
                user.setProviderId(googleId);
                user.setOauth2Email(email);
                user.setUpdatedAt(LocalDateTime.now().withNano(0));
            }

            // Cek apakah user sudah punya profile
            if (user.getProfile() == null) {
                createUserProfile(user, oAuth2User);
            }
        } else {
            // User baru - register
            user = registerNewUser(provider, googleId, email, name, oAuth2User);
        }
        
        // 5. Update last login
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        // 6. Return OAuth2User dengan authorities
        return new DefaultOAuth2User(
            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
            attributes,
            "email" // nameAttributeKey
        );
    }

    /** Mendaftarkan user baru yang login melalui OAuth2. */
    private Users registerNewUser(Users.AuthProvider provider, String providerId, String email, String name, OAuth2User oAuth2User) {
        Users user = new Users();
        
        // Generate username dari email
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;
        
        // Cari username yang belum digunakan
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        
        user.setEmail(email);
        user.setUsername(username);
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setOauth2Email(email);
        user.setStatus(Users.UserStatus.ACTIVE);
        user.setKycStatus(Users.KycStatus.NOT_SUBMITTED);
        user.setRole(Users.UserRole.USER);
        user.setCreatedAt(LocalDateTime.now().withNano(0));
        user.setUpdatedAt(LocalDateTime.now().withNano(0));
        
        Users savedUser = userRepository.save(user);

        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);

        // Split nama dari google
        if (name != null && !name.isEmpty()) {
            String[] namePart = name.split(" ", 2);
            profile.setFirstName(namePart[0]);
            if (namePart.length > 1) {
                profile.setLastName(namePart[1]);
            }

        } else {
            String emailPart = email.split("@")[0];
            profile.setFirstName(emailPart);
        }

        Map<String, Object> attributes = oAuth2User.getAttributes();
        String pictureUrl = (String) attributes.get("picture");
        if (pictureUrl != null) {
            profile.setProfileImageUrl(pictureUrl);
        }

        profile.setUpdatedAt(LocalDateTime.now().withNano(0));

        userProfileRepository.save(profile);

        return savedUser;
    }

    /** Membuat UserProfile dari data OAuth2 */
    private void createUserProfile(Users user, OAuth2User oAuth2User) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        UserProfile profile = new UserProfile();
        profile.setUser(user);

        String name = (String) attributes.get("name");
        String email = user.getEmail();

        // Split nama dari Google
        if (name != null && !name.isEmpty()) {
            String[] namePart = name.split(" ", 2);
            profile.setFirstName(namePart[0]);
            if (namePart.length > 1) {
                profile.setLastName(namePart[1]);
            }
        } else {
            String emailPart = email.split("@")[0];
            profile.setFirstName(emailPart);
        }

        // Set profile picture dari Google jika ada
        String pictureUrl = (String) attributes.get("picture");
        if (pictureUrl != null && !pictureUrl.isEmpty()) {
            profile.setProfileImageUrl(pictureUrl);
        }

        // Extract additional data jika ada
        String givenName = (String) attributes.get("given_name");
        String familyName = (String) attributes.get("family_name");

        if (givenName != null && !givenName.isEmpty()) {
            profile.setFirstName(givenName);
        }
        if (familyName != null && !familyName.isEmpty()) {
            profile.setLastName(familyName);
        }

        profile.setUpdatedAt(LocalDateTime.now().withNano(0));

        userProfileRepository.save(profile);
    }
    
}