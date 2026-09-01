package com.springtest.webchatsocket.model.dto.api;

import java.util.List;

public class ChatMembersResponse {

    private String roomId;

    private int onlineCount;

    private int sessionCount;

    private List<ChatMemberResponse> members;

    public ChatMembersResponse() {}

    public ChatMembersResponse(String roomId, int onlineCount, int sessionCount,
            List<ChatMemberResponse> members) {
        this.roomId = roomId;
        this.onlineCount = onlineCount;
        this.sessionCount = sessionCount;
        this.members = members;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public void setSessionCount(int sessionCount) {
        this.sessionCount = sessionCount;
    }

    public List<ChatMemberResponse> getMembers() {
        return members;
    }

    public void setMembers(List<ChatMemberResponse> members) {
        this.members = members;
    }
}
