package com.springtest.webchatsocket.model.dto.ws;

import java.util.Map;

public class ServerPushResponse {

    private String eventId;

    private String eventType;

    private Map<String, Object> payload;

    private String correlationId;

    private String timestamp;

    public ServerPushResponse() {}

    public ServerPushResponse(String eventId, String eventType, Map<String, Object> payload,
            String correlationId, String timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.correlationId = correlationId;
        this.timestamp = timestamp;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
