package com.springtest.webchatapi.model.dto.api;

import java.util.Map;

public record ChatEventResponse(String eventId, String eventType, String roomId, String userId,
        String username, String message, Map<String, Object> payload, String occurredAt) {
}
