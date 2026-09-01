package com.springtest.webchatsocket.websocket.listener;

import java.security.Principal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import com.springtest.webchatsocket.config.SocketNodeIdentity;
import com.springtest.webchatsocket.model.dto.api.ChatEventResponse;
import com.springtest.webchatsocket.model.dto.event.ServerPushEvent;
import com.springtest.webchatsocket.security.AuthenticatedPrincipal;
import com.springtest.webchatsocket.service.ChatEventHistoryService;
import com.springtest.webchatsocket.service.OnlineSessionsService;
import com.springtest.webchatsocket.service.OnlineSessionsService.PresenceChange;
import com.springtest.webchatsocket.service.ServerPushEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WebSocketSessionEventListener {

    private static final String PUBLIC_CHAT_DESTINATION = "/topic/chat.public";

    private final OnlineSessionsService onlineSessionsService;
    private final ServerPushEventPublisher serverPushEventPublisher;
    private final ChatEventHistoryService chatEventHistoryService;
    private final SocketNodeIdentity socketNodeIdentity;

    public WebSocketSessionEventListener(OnlineSessionsService onlineSessionsService,
            ServerPushEventPublisher serverPushEventPublisher,
            ChatEventHistoryService chatEventHistoryService,
            SocketNodeIdentity socketNodeIdentity) {
        this.onlineSessionsService = onlineSessionsService;
        this.serverPushEventPublisher = serverPushEventPublisher;
        this.chatEventHistoryService = chatEventHistoryService;
        this.socketNodeIdentity = socketNodeIdentity;
    }

    @EventListener
    public void handleSessionConnect(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        if (!StringUtils.hasText(sessionId)) {
            log.warn("event=websocket_connected_missing_session_id");
            return;
        }

        Principal user = accessor.getUser();
        String userId = user == null ? null : user.getName();
        String username = user instanceof AuthenticatedPrincipal authenticatedPrincipal
                ? authenticatedPrincipal.username()
                : userId;
        Instant connectedAt = Instant.now();
        String nodeId = socketNodeIdentity.nodeId();
        PresenceChange presenceChange =
                onlineSessionsService.register(sessionId, userId, username, connectedAt, nodeId);
        publishPresenceChange(presenceChange);
        log.info(
                "event=websocket_connected sessionId={} userId={} nodeId={} connectedAt={} onlineCount={}",
                sessionId, userId, nodeId, connectedAt, onlineSessionsService.getOnlineCount());
    }

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        if (!StringUtils.hasText(sessionId)) {
            log.warn("event=websocket_disconnected_missing_session_id");
            return;
        }

        PresenceChange presenceChange = onlineSessionsService.unregister(sessionId);
        publishPresenceChange(presenceChange);
        CloseStatus closeStatus = event.getCloseStatus();
        log.info("event=websocket_disconnected sessionId={} closeStatus={} onlineCount={}",
                sessionId, closeStatus, onlineSessionsService.getOnlineCount());
    }

    private void publishPresenceChange(PresenceChange presenceChange) {
        if (presenceChange == null || !presenceChange.hasEvent()) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("roomId", ChatEventHistoryService.PUBLIC_ROOM_ID);
        payload.put("userId", presenceChange.userId());
        payload.put("username", presenceChange.username());
        payload.put("timestamp", presenceChange.occurredAt().toString());

        ServerPushEvent event = serverPushEventPublisher.publishBroadcast(
                presenceChange.eventType(), PUBLIC_CHAT_DESTINATION, payload, null);
        chatEventHistoryService.record(new ChatEventResponse(event.eventId(), event.eventType(),
                ChatEventHistoryService.PUBLIC_ROOM_ID, presenceChange.userId(),
                presenceChange.username(), null, payload, event.occurredAt().toString()));
    }

}
