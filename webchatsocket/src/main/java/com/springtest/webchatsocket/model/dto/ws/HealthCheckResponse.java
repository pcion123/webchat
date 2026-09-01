package com.springtest.webchatsocket.model.dto.ws;

public class HealthCheckResponse {

    private String status;

    private String clientTime;

    private String serverTime;

    private String sessionId;

    private String nodeId;

    public HealthCheckResponse() {}

    public HealthCheckResponse(String status, String clientTime, String serverTime,
            String sessionId, String nodeId) {
        this.status = status;
        this.clientTime = clientTime;
        this.serverTime = serverTime;
        this.sessionId = sessionId;
        this.nodeId = nodeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getClientTime() {
        return clientTime;
    }

    public void setClientTime(String clientTime) {
        this.clientTime = clientTime;
    }

    public String getServerTime() {
        return serverTime;
    }

    public void setServerTime(String serverTime) {
        this.serverTime = serverTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

}
