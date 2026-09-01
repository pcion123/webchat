package com.springtest.webchatapi.service;

import java.time.Instant;
import java.util.Map;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import com.github.f4b6a3.uuid.UuidCreator;
import com.springtest.webchatapi.model.dto.event.ServerPushEvent;

@Service
public class ServerPushEventPublisher {

    private static final String TARGET_TYPE_USER = "USER";
    private static final String TARGET_TYPE_BROADCAST = "BROADCAST";

    private final RabbitTemplate rabbitTemplate;
    private final String exchangeName;
    private final String routingPrefix;
    private final String defaultDestination;

    public ServerPushEventPublisher(RabbitTemplate rabbitTemplate,
            @Value("${webchat.rabbitmq.server-push.exchange:webchat.server-push.topic}") String exchangeName,
            @Value("${webchat.rabbitmq.server-push.routing-prefix:server-push.webchatsocket}") String routingPrefix,
            @Value("${webchat.rabbitmq.server-push.default-destination:/queue/server-push}") String defaultDestination) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchangeName = exchangeName;
        this.routingPrefix = routingPrefix;
        this.defaultDestination = defaultDestination;
    }

    public void publishToUser(String userId, String eventType, Map<String, Object> payload,
            String correlationId) {
        publish(new ServerPushEvent(newId(), eventType, TARGET_TYPE_USER, userId,
                defaultDestination, payload, correlationId, Instant.now()));
    }

    public ServerPushEvent publishBroadcast(String eventType, String destination,
            Map<String, Object> payload, String correlationId) {
        ServerPushEvent event = new ServerPushEvent(newId(), eventType, TARGET_TYPE_BROADCAST, null,
                destination, payload, correlationId, Instant.now());
        publish(event);
        return event;
    }

    public void publish(ServerPushEvent event) {
        validate(event);
        rabbitTemplate.convertAndSend(exchangeName, routingKey(event.eventType()), event);
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
        if (TARGET_TYPE_USER.equals(event.targetType()) && !StringUtils.hasText(event.userId())) {
            throw new IllegalArgumentException("Server push event is missing required fields");
        }
        if (!TARGET_TYPE_USER.equals(event.targetType())
                && !TARGET_TYPE_BROADCAST.equals(event.targetType())) {
            throw new IllegalArgumentException("Server push event target type is not supported");
        }
    }

    private String newId() {
        return UuidCreator.getTimeOrderedEpoch().toString().replace("-", "");
    }
}
