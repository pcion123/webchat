package com.springtest.webchatsocket.model.dto.api;

public class OnlineSessionResponse {

    private String sessionId;

    private String connectedAt;

    private String nodeId;

    public OnlineSessionResponse() {}

    public OnlineSessionResponse(String sessionId, String connectedAt, String nodeId) {
        this.sessionId = sessionId;
        this.connectedAt = connectedAt;
        this.nodeId = nodeId;
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}
