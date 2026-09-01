package com.springtest.webchatsocket.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatsocket.model.dto.api.ChatMemberResponse;
import com.springtest.webchatsocket.model.dto.api.ChatMembersResponse;
import com.springtest.webchatsocket.model.dto.api.OnlineCountResponse;
import com.springtest.webchatsocket.model.dto.api.OnlineSessionResponse;
import com.springtest.webchatsocket.model.dto.api.OnlineSessionsResponse;
import com.springtest.webchatsocket.service.ChatEventHistoryService;
import com.springtest.webchatsocket.service.OnlineSessionsService;

@RestController
@RequestMapping("/api")
public class OnlineUsersController {

    private final OnlineSessionsService onlineSessionsService;

    public OnlineUsersController(OnlineSessionsService onlineSessionsService) {
        this.onlineSessionsService = onlineSessionsService;
    }

    @GetMapping("/online-count")
    public OnlineCountResponse getOnlineCount() {
        return new OnlineCountResponse(onlineSessionsService.getOnlineCount());
    }

    @GetMapping("/online-users")
    public OnlineSessionsResponse getOnlineUsers() {
        List<OnlineSessionResponse> sessions = onlineSessionsService.getOnlineSessions();
        return new OnlineSessionsResponse(sessions.size(), sessions);
    }

    @GetMapping("/chat/members")
    public ChatMembersResponse getChatMembers() {
        List<ChatMemberResponse> members = onlineSessionsService.getOnlineMembers();
        return new ChatMembersResponse(ChatEventHistoryService.PUBLIC_ROOM_ID, members.size(),
                onlineSessionsService.getOnlineCount(), members);
    }

}
