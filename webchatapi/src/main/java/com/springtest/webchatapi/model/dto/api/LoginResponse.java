package com.springtest.webchatapi.model.dto.api;

import java.time.Instant;
import java.time.LocalDateTime;

public record LoginResponse(String userId, String username, LocalDateTime lastLoginTime,
        String accessToken, String tokenType, Instant expiresAt) {
}
