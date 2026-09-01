package com.springtest.webchatapi.model.dto.api;

public record ChatMemberResponse(String userId, String username, String connectedAt,
        int sessionCount) {
}
