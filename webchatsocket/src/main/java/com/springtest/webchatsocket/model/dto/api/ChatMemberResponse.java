package com.springtest.webchatsocket.model.dto.api;

public class ChatMemberResponse {

    private String userId;

    private String username;

    private String connectedAt;

    private int sessionCount;

    public ChatMemberResponse() {}

    public ChatMemberResponse(String userId, String username, String connectedAt,
            int sessionCount) {
        this.userId = userId;
        this.username = username;
        this.connectedAt = connectedAt;
        this.sessionCount = sessionCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getConnectedAt() {
        return connectedAt;
    }

    public void setConnectedAt(String connectedAt) {
        this.connectedAt = connectedAt;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }
}
