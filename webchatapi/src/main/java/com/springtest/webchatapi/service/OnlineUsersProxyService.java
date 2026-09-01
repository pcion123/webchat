package com.springtest.webchatapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import com.springtest.webchatapi.model.dto.api.ChatMembersResponse;
import com.springtest.webchatapi.model.dto.api.OnlineCountResponse;
import com.springtest.webchatapi.model.dto.api.OnlineSessionsResponse;

@Service
public class OnlineUsersProxyService {

    private final RestClient restClient;

    public OnlineUsersProxyService(
            @Value("${webchat.socket-api.base-url:http://localhost:9092}") String socketApiBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(normalizeBaseUrl(socketApiBaseUrl)).build();
    }

    public OnlineCountResponse getOnlineCount() {
        return restClient.get().uri("/api/online-count").retrieve().body(OnlineCountResponse.class);
    }

    public OnlineSessionsResponse getOnlineUsers() {
        return restClient.get().uri("/api/online-users").retrieve()
                .body(OnlineSessionsResponse.class);
    }

    public ChatMembersResponse getChatMembers() {
        return restClient.get().uri("/api/chat/members").retrieve().body(ChatMembersResponse.class);
    }

    private String normalizeBaseUrl(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            throw new IllegalArgumentException("webchat.socket-api.base-url must not be blank");
        }
        return rawValue.endsWith("/") ? rawValue.substring(0, rawValue.length() - 1) : rawValue;
    }
}
