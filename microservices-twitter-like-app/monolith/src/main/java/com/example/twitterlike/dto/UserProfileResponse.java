package com.example.twitterlike.dto;

import java.time.Instant;

public class UserProfileResponse {

    private String auth0UserId;
    private String displayName;
    private String email;
    private Instant lastSeenAt;

    public UserProfileResponse() {
    }

    public UserProfileResponse(String auth0UserId, String displayName, String email, Instant lastSeenAt) {
        this.auth0UserId = auth0UserId;
        this.displayName = displayName;
        this.email = email;
        this.lastSeenAt = lastSeenAt;
    }

    public String getAuth0UserId() {
        return auth0UserId;
    }

    public void setAuth0UserId(String auth0UserId) {
        this.auth0UserId = auth0UserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
