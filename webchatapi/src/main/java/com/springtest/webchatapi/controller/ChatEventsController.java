package com.springtest.webchatapi.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatapi.model.dto.api.ChatEventsResponse;
import com.springtest.webchatapi.security.JwsTokenValidator;
import com.springtest.webchatapi.service.ChatEventHistoryService;
import com.springtest.webchatapi.service.ChatEventHistoryService.ChatEventsResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/chat/events")
public class ChatEventsController {

    private final ChatEventHistoryService chatEventHistoryService;
    private final JwsTokenValidator jwsTokenValidator;

    public ChatEventsController(ChatEventHistoryService chatEventHistoryService,
            JwsTokenValidator jwsTokenValidator) {
        this.chatEventHistoryService = chatEventHistoryService;
        this.jwsTokenValidator = jwsTokenValidator;
    }

    @GetMapping
    public ChatEventsResponse getChatEvents(@RequestParam long requestId,
            @RequestParam(defaultValue = "100") @Min(1) @Max(100) int limit,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION,
                    required = false) String authorization) {
        jwsTokenValidator.validateAuthorizationHeader(authorization);
        ChatEventsResult result = chatEventHistoryService.getRecentEvents(limit);
        return new ChatEventsResponse(ChatEventHistoryService.PUBLIC_ROOM_ID, result.limit(),
                result.events());
    }
}
