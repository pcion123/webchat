package com.springtest.webchatapi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import com.springtest.webchatapi.model.dto.api.ChatEventResponse;
import com.springtest.webchatapi.repository.RedisDao;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
public class ChatEventHistoryService {

    public static final String PUBLIC_ROOM_ID = "public";

    private static final String PUBLIC_ROOM_EVENTS_KEY = "webchat:room:public:events";
    private static final int MAX_HISTORY_SIZE = 100;

    private final RedisDao redisDao;
    private final ObjectMapper objectMapper;

    public ChatEventHistoryService(RedisDao redisDao, ObjectMapper objectMapper) {
        this.redisDao = redisDao;
        this.objectMapper = objectMapper;
    }

    public void record(ChatEventResponse event) {
        if (event == null) {
            return;
        }
        try {
            redisDao.leftPushAndTrim(PUBLIC_ROOM_EVENTS_KEY, objectMapper.writeValueAsString(event),
                    MAX_HISTORY_SIZE);
        } catch (Exception exception) {
            log.warn("event=chat_history_record_failed eventId={} eventType={}", event.eventId(),
                    event.eventType(), exception);
        }
    }

    public ChatEventsResult getRecentEvents(int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, MAX_HISTORY_SIZE));
        List<String> values = redisDao.range(PUBLIC_ROOM_EVENTS_KEY, 0, normalizedLimit - 1L);
        List<ChatEventResponse> events = new ArrayList<>();
        for (String value : values) {
            try {
                events.add(objectMapper.readValue(value, ChatEventResponse.class));
            } catch (Exception exception) {
                log.warn("event=chat_history_parse_failed", exception);
            }
        }
        Collections.reverse(events);
        return new ChatEventsResult(normalizedLimit, events);
    }

    public record ChatEventsResult(int limit, List<ChatEventResponse> events) {
    }
}
