package com.springtest.webchatapi.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatapi.model.dto.api.ChatMembersResponse;
import com.springtest.webchatapi.security.JwsTokenValidator;
import com.springtest.webchatapi.service.OnlineUsersProxyService;

@Validated
@RestController
@RequestMapping("/api/chat/members")
public class ChatMembersController {

    private final OnlineUsersProxyService onlineUsersProxyService;
    private final JwsTokenValidator jwsTokenValidator;

    public ChatMembersController(OnlineUsersProxyService onlineUsersProxyService,
            JwsTokenValidator jwsTokenValidator) {
        this.onlineUsersProxyService = onlineUsersProxyService;
        this.jwsTokenValidator = jwsTokenValidator;
    }

    @GetMapping
    public ChatMembersResponse getChatMembers(@RequestParam long requestId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION,
                    required = false) String authorization) {
        jwsTokenValidator.validateAuthorizationHeader(authorization);
        return onlineUsersProxyService.getChatMembers();
    }
}
