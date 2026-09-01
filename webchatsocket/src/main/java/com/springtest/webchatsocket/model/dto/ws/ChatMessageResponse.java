package com.springtest.webchatsocket.model.dto.ws;

public class ChatMessageResponse {

    private String messageId;

    private String senderUserId;

    private String senderUsername;

    private String message;

    private String timestamp;

    public ChatMessageResponse() {}

    public ChatMessageResponse(String messageId, String senderUserId, String senderUsername,
            String message, String timestamp) {
        this.messageId = messageId;
        this.senderUserId = senderUserId;
        this.senderUsername = senderUsername;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getSenderUserId() {
        return senderUserId;
    }

    public void setSenderUserId(String senderUserId) {
        this.senderUserId = senderUserId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

}
