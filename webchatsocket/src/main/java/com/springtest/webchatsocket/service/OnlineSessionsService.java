package com.springtest.webchatsocket.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.springtest.webchatsocket.model.dto.api.ChatMemberResponse;
import com.springtest.webchatsocket.model.dto.api.OnlineSessionResponse;

@Service
public class OnlineSessionsService {

    public static final String EVENT_TYPE_USER_JOINED = "chat-user-joined";
    public static final String EVENT_TYPE_USER_LEFT = "chat-user-left";
    private static final String DEFAULT_NODE_ID = "local";

    private final ConcurrentMap<String, OnlineSession> onlineSessions = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();

    public PresenceChange register(String sessionId, Instant connectedAt) {
        return register(sessionId, null, null, connectedAt);
    }

    public PresenceChange register(String sessionId, String userId, Instant connectedAt) {
        return register(sessionId, userId, null, connectedAt);
    }

    public PresenceChange register(String sessionId, String userId, String username,
            Instant connectedAt) {
        return register(sessionId, userId, username, connectedAt, DEFAULT_NODE_ID);
    }

    public PresenceChange register(String sessionId, String userId, String username,
            Instant connectedAt, String nodeId) {
        if (!StringUtils.hasText(sessionId)) {
            return PresenceChange.none();
        }
        String normalizedUserId = normalizeUserId(userId);
        String normalizedUsername = normalizeUsername(username, normalizedUserId);
        String normalizedNodeId = normalizeNodeId(nodeId);
        OnlineSession previousSession = onlineSessions.put(sessionId, new OnlineSession(sessionId,
                normalizedUserId, normalizedUsername, connectedAt, normalizedNodeId));
        if (previousSession != null) {
            removeUserSession(previousSession.userId(), sessionId);
        }
        boolean userWasOnline = hasUser(normalizedUserId);
        if (StringUtils.hasText(normalizedUserId)) {
            userSessions.computeIfAbsent(normalizedUserId, key -> ConcurrentHashMap.newKeySet())
                    .add(sessionId);
        }
        if (StringUtils.hasText(normalizedUserId) && !userWasOnline) {
            return new PresenceChange(EVENT_TYPE_USER_JOINED, normalizedUserId, normalizedUsername,
                    connectedAt);
        }
        return PresenceChange.none();
    }

    public PresenceChange unregister(String sessionId) {
        if (!StringUtils.hasText(sessionId)) {
            return PresenceChange.none();
        }
        OnlineSession removedSession = onlineSessions.remove(sessionId);
        if (removedSession != null) {
            removeUserSession(removedSession.userId(), sessionId);
            if (StringUtils.hasText(removedSession.userId()) && !hasUser(removedSession.userId())) {
                return new PresenceChange(EVENT_TYPE_USER_LEFT, removedSession.userId(),
                        removedSession.username(), Instant.now());
            }
        }
        return PresenceChange.none();
    }

    public boolean hasUser(String userId) {
        String normalizedUserId = normalizeUserId(userId);
        if (!StringUtils.hasText(normalizedUserId)) {
            return false;
        }
        Set<String> sessions = userSessions.get(normalizedUserId);
        return sessions != null && !sessions.isEmpty();
    }

    public int getOnlineCount() {
        return onlineSessions.size();
    }

    public List<OnlineSessionResponse> getOnlineSessions() {
        return onlineSessions.values().stream()
                .sorted(Comparator.comparing(OnlineSession::connectedAt))
                .map(session -> new OnlineSessionResponse(session.sessionId(),
                        session.connectedAt().toString(), session.nodeId()))
                .toList();
    }

    public List<ChatMemberResponse> getOnlineMembers() {
        return userSessions.entrySet().stream()
                .map(entry -> memberResponse(entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ChatMemberResponse::getConnectedAt)).toList();
    }

    private void removeUserSession(String userId, String sessionId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        userSessions.computeIfPresent(userId, (key, sessions) -> {
            sessions.remove(sessionId);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    private String normalizeUserId(String userId) {
        return StringUtils.hasText(userId) ? userId.trim() : null;
    }

    private String normalizeUsername(String username, String userId) {
        if (StringUtils.hasText(username)) {
            return username.trim();
        }
        return userId;
    }

    private String normalizeNodeId(String nodeId) {
        return StringUtils.hasText(nodeId) ? nodeId.trim() : DEFAULT_NODE_ID;
    }

    private ChatMemberResponse memberResponse(String userId, Set<String> sessionIds) {
        List<OnlineSession> sessions =
                sessionIds.stream().map(onlineSessions::get).filter(Objects::nonNull).toList();
        if (sessions.isEmpty()) {
            return null;
        }
        OnlineSession firstSession = sessions.stream()
                .min(Comparator.comparing(OnlineSession::connectedAt)).orElseThrow();
        String username = sessions.stream().map(OnlineSession::username)
                .filter(StringUtils::hasText).findFirst().orElse(userId);
        return new ChatMemberResponse(userId, username, firstSession.connectedAt().toString(),
                sessions.size());
    }

    public record PresenceChange(String eventType, String userId, String username,
            Instant occurredAt) {

        public static PresenceChange none() {
            return new PresenceChange(null, null, null, null);
        }

        public boolean hasEvent() {
            return StringUtils.hasText(eventType) && StringUtils.hasText(userId)
                    && occurredAt != null;
        }
    }

    private record OnlineSession(String sessionId, String userId, String username,
            Instant connectedAt, String nodeId) {
    }

}
