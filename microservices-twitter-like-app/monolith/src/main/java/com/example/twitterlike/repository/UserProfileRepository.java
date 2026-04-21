package com.example.twitterlike.repository;

import com.example.twitterlike.entity.UserProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByAuth0UserId(String auth0UserId);
}
