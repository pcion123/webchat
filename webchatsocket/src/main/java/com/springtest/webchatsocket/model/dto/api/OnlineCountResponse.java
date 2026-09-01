package com.springtest.webchatsocket.model.dto.api;

public class OnlineCountResponse {

    private int onlineCount;

    public OnlineCountResponse() {}

    public OnlineCountResponse(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

}
