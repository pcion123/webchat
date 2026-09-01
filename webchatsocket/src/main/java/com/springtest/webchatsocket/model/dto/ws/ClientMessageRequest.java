package com.springtest.webchatsocket.model.dto.ws;

public class ClientMessageRequest {

    private String message;

    public ClientMessageRequest() {}

    public ClientMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
