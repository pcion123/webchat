package com.springtest.webchatsocket.websocket.controller;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import com.springtest.webchatsocket.config.SocketNodeIdentity;
import com.springtest.webchatsocket.model.dto.ws.HealthCheckRequest;
import com.springtest.webchatsocket.model.dto.ws.HealthCheckResponse;
import com.springtest.webchatsocket.service.MessageService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class HealthCheckController {

    private final MessageService messageService;
    private final SocketNodeIdentity socketNodeIdentity;

    public HealthCheckController(MessageService messageService,
            SocketNodeIdentity socketNodeIdentity) {
        this.messageService = messageService;
        this.socketNodeIdentity = socketNodeIdentity;
    }

    @MessageMapping("/healthcheck")
    @SendToUser(value = "/queue/healthcheck", broadcast = false)
    public HealthCheckResponse healthCheck(HealthCheckRequest request,
            @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId) {
        String clientTime = request == null ? null : request.getClientTime();
        log.info("Received health check request with clientTime: {}", clientTime);
        return messageService.createHealthCheck(clientTime, sessionId, socketNodeIdentity.nodeId());
    }

}
