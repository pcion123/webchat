package com.springtest.webchatsocket.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import com.springtest.webchatsocket.config.SocketNodeIdentity;
import com.springtest.webchatsocket.model.dto.event.ServerPushEvent;
import com.springtest.webchatsocket.model.dto.ws.ServerPushResponse;
import com.springtest.webchatsocket.service.MessageService;
import com.springtest.webchatsocket.service.OnlineSessionsService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@ConditionalOnProperty(name = "webchat.rabbitmq.server-push.enabled", havingValue = "true",
                matchIfMissing = true)
public class ServerPushRabbitListener {

        private static final String DEFAULT_DESTINATION = "/queue/server-push";
        private static final String TARGET_TYPE_USER = "USER";
        private static final String TARGET_TYPE_BROADCAST = "BROADCAST";

        private final OnlineSessionsService onlineSessionsService;
        private final MessageService messageService;
        private final SimpMessagingTemplate messagingTemplate;
        private final String nodeId;

        public ServerPushRabbitListener(OnlineSessionsService onlineSessionsService,
                        MessageService messageService, SimpMessagingTemplate messagingTemplate,
                        SocketNodeIdentity socketNodeIdentity) {
                this.onlineSessionsService = onlineSessionsService;
                this.messageService = messageService;
                this.messagingTemplate = messagingTemplate;
                this.nodeId = socketNodeIdentity.nodeId();
        }

        @RabbitListener(queues = "#{serverPushQueue.name}",
                        autoStartup = "${webchat.rabbitmq.server-push.listener-auto-startup:true}")
        public void handleServerPush(ServerPushEvent event) {
                if (!isValid(event)) {
                        log.warn("event=server_push_invalid nodeId={}", nodeId);
                        return;
                }

                String destination = StringUtils.hasText(event.destination()) ? event.destination()
                                : DEFAULT_DESTINATION;
                ServerPushResponse response = messageService.createServerPushResponse(event);

                if (TARGET_TYPE_BROADCAST.equals(event.targetType())) {
                        messagingTemplate.convertAndSend(destination, response);
                        log.info("event=server_push_broadcast_sent eventId={} eventType={} destination={} nodeId={}",
                                        event.eventId(), event.eventType(), destination, nodeId);
                        return;
                }

                if (!onlineSessionsService.hasUser(event.userId())) {
                        log.info("event=server_push_skipped eventId={} eventType={} userId={} nodeId={} reason=no-local-session",
                                        event.eventId(), event.eventType(), event.userId(), nodeId);
                        return;
                }

                messagingTemplate.convertAndSendToUser(event.userId(), destination, response);
                log.info("event=server_push_sent eventId={} eventType={} userId={} nodeId={}",
                                event.eventId(), event.eventType(), event.userId(), nodeId);
        }

        private boolean isValid(ServerPushEvent event) {
                if (event == null || !StringUtils.hasText(event.eventId())
                                || !StringUtils.hasText(event.eventType())
                                || !StringUtils.hasText(event.targetType())) {
                        return false;
                }
                if (TARGET_TYPE_USER.equals(event.targetType())) {
                        return StringUtils.hasText(event.userId());
                }
                if (TARGET_TYPE_BROADCAST.equals(event.targetType())) {
                        return StringUtils.hasText(event.destination());
                }
                return false;
        }
}
