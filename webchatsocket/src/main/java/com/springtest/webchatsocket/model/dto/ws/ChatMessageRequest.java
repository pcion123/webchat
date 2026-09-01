package com.springtest.webchatsocket.model.dto.ws;

public class ChatMessageRequest {

    private Long requestId;

    private String message;

    private String clientTime;

    public ChatMessageRequest() {}

    public ChatMessageRequest(Long requestId, String message, String clientTime) {
        this.requestId = requestId;
        this.message = message;
        this.clientTime = clientTime;
    }

    public Long getRequestId() {
        return requestId;
    }

    public void setRequestId(Long requestId) {
        this.requestId = requestId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getClientTime() {
        return clientTime;
    }

    public void setClientTime(String clientTime) {
        this.clientTime = clientTime;
    }

}
