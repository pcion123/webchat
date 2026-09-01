package com.springtest.webchatsocket.model.dto.ws;

public class HealthCheckRequest {

    private String clientTime;

    public HealthCheckRequest() {}

    public HealthCheckRequest(String clientTime) {
        this.clientTime = clientTime;
    }

    public String getClientTime() {
        return clientTime;
    }

    public void setClientTime(String clientTime) {
        this.clientTime = clientTime;
    }

}
