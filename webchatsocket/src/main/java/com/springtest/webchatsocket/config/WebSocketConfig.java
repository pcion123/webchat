package com.springtest.webchatsocket.config;

import java.util.Map;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;
import com.springtest.webchatsocket.security.AuthenticatedPrincipal;
import com.springtest.webchatsocket.security.JwsTokenValidator;
import com.springtest.webchatsocket.security.JwsTokenValidator.AuthenticatedUser;
import com.springtest.webchatsocket.security.JwsTokenValidator.TokenValidationException;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private static final String AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser";

    private final JwsTokenValidator jwsTokenValidator;

    public WebSocketConfig(JwsTokenValidator jwsTokenValidator) {
        this.jwsTokenValidator = jwsTokenValidator;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").addInterceptors(new TokenHandshakeInterceptor())
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    AuthenticatedUser authenticatedUser = authenticatedUser(accessor);
                    accessor.setUser(new AuthenticatedPrincipal(authenticatedUser.userId(),
                            authenticatedUser.username()));
                }
                return message;
            }
        });
    }

    private AuthenticatedUser authenticatedUser(StompHeaderAccessor accessor) {
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            Object authenticatedUser = sessionAttributes.get(AUTHENTICATED_USER_ATTRIBUTE);
            if (authenticatedUser instanceof AuthenticatedUser user) {
                return user;
            }
        }
        try {
            return jwsTokenValidator.validateAuthorizationHeader(
                    accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION));
        } catch (TokenValidationException exception) {
            throw new MessagingException("Unauthorized WebSocket connection", exception);
        }
    }

    private class TokenHandshakeInterceptor implements HandshakeInterceptor {

        @Override
        public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                WebSocketHandler wsHandler, Map<String, Object> attributes) {
            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authorizationHeader == null) {
                return true;
            }
            try {
                attributes.put(AUTHENTICATED_USER_ATTRIBUTE,
                        jwsTokenValidator.validateAuthorizationHeader(authorizationHeader));
                return true;
            } catch (TokenValidationException exception) {
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }
        }

        @Override
        public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                WebSocketHandler wsHandler, Exception exception) {}

    }

}
