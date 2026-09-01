package com.springtest.webchatapi.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.github.f4b6a3.uuid.UuidCreator;
import com.springtest.webchatapi.exception.InvalidRequestException;
import com.springtest.webchatapi.model.dto.api.ChatEventResponse;
import com.springtest.webchatapi.model.dto.api.SendMessageRequest;
import com.springtest.webchatapi.model.dto.api.SendMessageResponse;
import com.springtest.webchatapi.model.dto.event.ServerPushEvent;
import com.springtest.webchatapi.security.JwsTokenValidator.AuthenticatedUser;

@Service
public class MessageService {

    private static final String EVENT_TYPE_CHAT_MESSAGE_CREATED = "chat-message-created";
    private static final String PUBLIC_CHAT_DESTINATION = "/topic/chat.public";

    private final ServerPushEventPublisher serverPushEventPublisher;
    private final ChatEventHistoryService chatEventHistoryService;

    @Autowired
    public MessageService(ServerPushEventPublisher serverPushEventPublisher,
            ChatEventHistoryService chatEventHistoryService) {
        this.serverPushEventPublisher = serverPushEventPublisher;
        this.chatEventHistoryService = chatEventHistoryService;
    }

    public MessageService(ServerPushEventPublisher serverPushEventPublisher) {
        this(serverPushEventPublisher, null);
    }

    public SendMessageResponse sendMessage(SendMessageRequest request, AuthenticatedUser sender) {
        String message = request == null ? null : request.getMessage();
        if (!StringUtils.hasText(message)) {
            throw new InvalidRequestException("Message cannot be empty");
        }
        String acceptedAt = Instant.now().toString();
        String messageId = newId();
        String correlationId = String.valueOf(request.getRequestId());
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messageId", messageId);
        payload.put("senderUserId", sender.userId());
        String senderUsername =
                StringUtils.hasText(sender.username()) ? sender.username() : sender.userId();
        String normalizedMessage = message.trim();
        payload.put("senderUsername", senderUsername);
        payload.put("message", normalizedMessage);
        payload.put("timestamp", acceptedAt);
        if (StringUtils.hasText(request.getClientTime())) {
            payload.put("clientTime", request.getClientTime());
        }

        ServerPushEvent event = serverPushEventPublisher.publishBroadcast(
                EVENT_TYPE_CHAT_MESSAGE_CREATED, PUBLIC_CHAT_DESTINATION, payload, correlationId);
        recordChatHistory(event, sender.userId(), senderUsername, normalizedMessage, payload);
        return new SendMessageResponse(messageId, event.eventId(), EVENT_TYPE_CHAT_MESSAGE_CREATED,
                correlationId, acceptedAt);
    }

    private void recordChatHistory(ServerPushEvent event, String senderUserId,
            String senderUsername, String message, Map<String, Object> payload) {
        if (chatEventHistoryService == null) {
            return;
        }
        chatEventHistoryService.record(new ChatEventResponse(event.eventId(), event.eventType(),
                ChatEventHistoryService.PUBLIC_ROOM_ID, senderUserId, senderUsername, message,
                new LinkedHashMap<>(payload), event.occurredAt().toString()));
    }

    private String newId() {
        return UuidCreator.getTimeOrderedEpoch().toString().replace("-", "");
    }
}
