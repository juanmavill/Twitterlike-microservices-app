package com.example.twitterlike.service;

import com.example.twitterlike.dto.UserProfileResponse;
import com.example.twitterlike.entity.UserProfile;
import com.example.twitterlike.repository.UserProfileRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Transactional
    public UserProfileResponse getCurrentUserProfile(Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("JWT subject is required to resolve current user");
        }

        String displayName = resolveDisplayName(jwt, subject);
        String email = jwt.getClaimAsString("email");

        UserProfile profile = userProfileRepository.findByAuth0UserId(subject)
                .orElseGet(UserProfile::new);

        profile.setAuth0UserId(subject);
        profile.setDisplayName(displayName);
        profile.setEmail(email);

        UserProfile savedProfile = userProfileRepository.save(profile);
        return new UserProfileResponse(
                savedProfile.getAuth0UserId(),
                savedProfile.getDisplayName(),
                savedProfile.getEmail(),
                savedProfile.getLastSeenAt()
        );
    }

    private String resolveDisplayName(Jwt jwt, String fallback) {
        String name = jwt.getClaimAsString("name");
        if (name != null && !name.isBlank()) {
            return name;
        }

        String preferredUsername = jwt.getClaimAsString("preferred_username");
        if (preferredUsername != null && !preferredUsername.isBlank()) {
            return preferredUsername;
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return email;
        }

        return fallback;
    }
}
