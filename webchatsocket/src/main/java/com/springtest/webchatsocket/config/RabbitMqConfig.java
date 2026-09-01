package com.springtest.webchatsocket.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RabbitMqConfig {

    private static final String DEFAULT_QUEUE_PREFIX = "webchatsocket.push";

    @Bean
    public TopicExchange serverPushExchange(
            @Value("${webchat.rabbitmq.server-push.exchange:webchat.server-push.topic}") String exchangeName) {
        return new TopicExchange(exchangeName, true, false);
    }

    @Bean
    public Queue serverPushQueue(
            @Value("${webchat.rabbitmq.server-push.queue-prefix:webchatsocket.push}") String queuePrefix,
            SocketNodeIdentity socketNodeIdentity) {
        String normalizedQueuePrefix =
                StringUtils.hasText(queuePrefix) ? queuePrefix.trim() : DEFAULT_QUEUE_PREFIX;
        return QueueBuilder.durable(normalizedQueuePrefix + "." + socketNodeIdentity.nodeId())
                .autoDelete().build();
    }

    @Bean
    public Binding serverPushBinding(Queue serverPushQueue, TopicExchange serverPushExchange,
            @Value("${webchat.rabbitmq.server-push.routing-pattern:server-push.webchatsocket.#}") String routingPattern) {
        return BindingBuilder.bind(serverPushQueue).to(serverPushExchange).with(routingPattern);
    }

    @Bean
    public MessageConverter rabbitMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
