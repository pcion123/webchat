package com.springtest.webchatsocket.service;

import org.springframework.stereotype.Service;
import com.springtest.webchatsocket.model.dto.api.ChatEventResponse;
import com.springtest.webchatsocket.repository.RedisDao;
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
}
