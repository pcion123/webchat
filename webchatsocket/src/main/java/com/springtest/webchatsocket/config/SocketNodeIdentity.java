package com.springtest.webchatsocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SocketNodeIdentity {

    private static final String DEFAULT_NODE_ID = "local";

    private final String nodeId;

    public SocketNodeIdentity(@Value("${webchat.socket.node-id:local}") String nodeId) {
        this.nodeId = StringUtils.hasText(nodeId) ? nodeId.trim() : DEFAULT_NODE_ID;
    }

    public String nodeId() {
        return nodeId;
    }

}
