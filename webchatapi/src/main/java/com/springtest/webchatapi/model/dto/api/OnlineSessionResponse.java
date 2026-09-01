package com.springtest.webchatapi.model.dto.api;

public class OnlineSessionResponse {

    private String sessionId;

    private String connectedAt;

    public OnlineSessionResponse() {}

    public OnlineSessionResponse(String sessionId, String connectedAt) {
        this.sessionId = sessionId;
        this.connectedAt = connectedAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }
}
