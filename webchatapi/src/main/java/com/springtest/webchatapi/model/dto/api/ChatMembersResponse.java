package com.springtest.webchatapi.model.dto.api;

import java.util.List;

public record ChatMembersResponse(String roomId, int onlineCount, int sessionCount,
        List<ChatMemberResponse> members) {
}
