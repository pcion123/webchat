package com.springtest.webchatapi.controller;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatapi.model.dto.api.OnlineCountResponse;
import com.springtest.webchatapi.model.dto.api.OnlineSessionsResponse;
import com.springtest.webchatapi.service.OnlineUsersProxyService;

@Validated
@RestController
@RequestMapping("/api")
public class OnlineUsersController {

    private final OnlineUsersProxyService onlineUsersProxyService;

    public OnlineUsersController(OnlineUsersProxyService onlineUsersProxyService) {
        this.onlineUsersProxyService = onlineUsersProxyService;
    }

    @GetMapping("/online-count")
    public OnlineCountResponse getOnlineCount() {
        return onlineUsersProxyService.getOnlineCount();
    }

    @GetMapping("/online-users")
    public OnlineSessionsResponse getOnlineUsers() {
        return onlineUsersProxyService.getOnlineUsers();
    }
}
