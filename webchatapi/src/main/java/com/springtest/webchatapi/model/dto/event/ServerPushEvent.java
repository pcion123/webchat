package com.springtest.webchatapi.model.dto.event;

import java.time.Instant;
import java.util.Map;

public record ServerPushEvent(String eventId, String eventType, String targetType, String userId,
                String destination, Map<String, Object> payload, String correlationId,
                Instant occurredAt) {
}
