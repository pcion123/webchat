package com.springtest.webchatapi.model.dto.api;

public record SendMessageResponse(String messageId, String eventId, String eventType,
        String correlationId, String acceptedAt) {
}
