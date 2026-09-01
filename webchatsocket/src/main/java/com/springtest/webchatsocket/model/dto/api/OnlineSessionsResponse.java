package com.springtest.webchatsocket.model.dto.api;

import java.util.List;

public class OnlineSessionsResponse {

    private int onlineCount;

    private List<OnlineSessionResponse> sessions;

    public OnlineSessionsResponse() {}

    public OnlineSessionsResponse(int onlineCount, List<OnlineSessionResponse> sessions) {
        this.onlineCount = onlineCount;
        this.sessions = sessions;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public List<OnlineSessionResponse> getSessions() {
        return sessions;
    }

    public void setSessions(List<OnlineSessionResponse> sessions) {
        this.sessions = sessions;
    }

}
