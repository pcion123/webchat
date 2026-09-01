package com.springtest.webchatsocket.service;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.springtest.webchatsocket.model.dto.event.ServerPushEvent;
import com.springtest.webchatsocket.model.dto.ws.ChatMessageRequest;
import com.springtest.webchatsocket.model.dto.ws.ChatMessageResponse;
import com.springtest.webchatsocket.model.dto.ws.HealthCheckResponse;
import com.springtest.webchatsocket.model.dto.ws.ReceivedMessageResponse;
import com.springtest.webchatsocket.model.dto.ws.ServerPushResponse;

@Service
public class MessageService {

    public ReceivedMessageResponse createReceivedMessage() {
        return new ReceivedMessageResponse("received", Instant.now().toString());
    }

    public ChatMessageResponse createChatMessage(ChatMessageRequest request, String senderUserId,
            String senderUsername) {
        String message = request == null ? null : request.getMessage();
        if (!StringUtils.hasText(message)) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        String displayName = StringUtils.hasText(senderUsername) ? senderUsername : senderUserId;
        return new ChatMessageResponse(UUID.randomUUID().toString(), senderUserId, displayName,
                message.trim(), Instant.now().toString());
    }

    public HealthCheckResponse createHealthCheck(String clientTime, String sessionId,
            String nodeId) {
        return new HealthCheckResponse("pong", clientTime, Instant.now().toString(), sessionId,
                nodeId);
    }

    public ServerPushResponse createServerPushResponse(ServerPushEvent event) {
        return new ServerPushResponse(event.eventId(), event.eventType(), event.payload(),
                event.correlationId(), Instant.now().toString());
    }

}
