package com.springtest.webchatapi.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatapi.model.dto.api.SendMessageRequest;
import com.springtest.webchatapi.model.dto.api.SendMessageResponse;
import com.springtest.webchatapi.security.JwsTokenValidator;
import com.springtest.webchatapi.security.JwsTokenValidator.AuthenticatedUser;
import com.springtest.webchatapi.service.MessageService;
import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final JwsTokenValidator jwsTokenValidator;

    public MessageController(MessageService messageService, JwsTokenValidator jwsTokenValidator) {
        this.messageService = messageService;
        this.jwsTokenValidator = jwsTokenValidator;
    }

    @PostMapping
    public ResponseEntity<SendMessageResponse> sendMessage(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION,
                    required = false) String authorization,
            @Valid @RequestBody SendMessageRequest request) {
        AuthenticatedUser sender = jwsTokenValidator.validateAuthorizationHeader(authorization);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(messageService.sendMessage(request, sender));
    }
}
