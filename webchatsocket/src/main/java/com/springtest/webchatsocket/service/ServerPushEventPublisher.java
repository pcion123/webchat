package com.springtest.webchatsocket.service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.springtest.webchatsocket.model.dto.event.ServerPushEvent;

@Service
public class ServerPushEventPublisher {

    private static final String TARGET_TYPE_BROADCAST = "BROADCAST";

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingPrefix;
    private final boolean enabled;

    public ServerPushEventPublisher(RabbitTemplate rabbitTemplate,
            @Value("${webchat.rabbitmq.server-push.exchange:webchat.server-push.topic}") String exchangeName,
            @Value("${webchat.rabbitmq.server-push.routing-prefix:server-push.webchatsocket}") String routingPrefix,
            @Value("${webchat.rabbitmq.server-push.enabled:true}") boolean enabled) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingPrefix = routingPrefix;
        this.enabled = enabled;
    }

    public ServerPushEvent publishBroadcast(String eventType, String destination,
            Map<String, Object> payload, String correlationId) {
        ServerPushEvent event = new ServerPushEvent(newId(), eventType, TARGET_TYPE_BROADCAST, null,
                destination, payload, correlationId, Instant.now());
        validate(event);
        if (enabled) {
            rabbitTemplate.convertAndSend(exchangeName, routingKey(event.eventType()), event);
        }
        return event;
    }

    private String routingKey(String eventType) {
        return routingPrefix + "." + eventType;
    }

    private void validate(ServerPushEvent event) {
        if (event == null || !StringUtils.hasText(event.eventId())
                || !StringUtils.hasText(event.eventType())
                || !StringUtils.hasText(event.targetType())
                || !StringUtils.hasText(event.destination()) || event.occurredAt() == null) {
            throw new IllegalArgumentException("Server push event is missing required fields");
        }
    }

    private String newId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
