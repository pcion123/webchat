export class WebchatStompClient {
    constructor(config, callbacks) {
        this.config = config;
        this.callbacks = callbacks;
        this.client = null;
        this.subscriptions = [];
        this.healthTimerId = null;
        this.healthTimeoutId = null;
        this.healthPending = null;
        this.healthSequence = 0;
    }

    connect(socketUrl, session) {
        this.assertStompLoaded();
        const brokerURL = this.validateSocketUrl(socketUrl);
        const authorization = `${session.tokenType || "Bearer"} ${session.accessToken}`;

        this.disconnect({ silent: true });
        this.callbacks.onStatus("connecting");
        this.callbacks.onLog("connecting", "Opening STOMP connection to " + brokerURL);

        this.client = new window.StompJs.Client({
            brokerURL,
            connectHeaders: { Authorization: authorization },
            reconnectDelay: 0,
            heartbeatIncoming: 10000,
            heartbeatOutgoing: 10000,
            debug: (line) => this.callbacks.onLog("debug", line),
            onConnect: () => this.handleConnect(),
            onStompError: (frame) => this.handleStompError(frame),
            onWebSocketError: () => this.handleWebSocketError(),
            onWebSocketClose: (event) => this.handleWebSocketClose(event),
            onDisconnect: () => this.handleDisconnect(),
        });

        this.client.activate();
    }

    async disconnect(options = {}) {
        this.stopHealthCheck();
        const activeClient = this.client;
        this.client = null;
        this.subscriptions = [];
        if (activeClient) {
            if (!options.silent) {
                this.callbacks.onStatus("disconnecting");
                this.callbacks.onLog("disconnecting", "Closing STOMP session.");
            }
            await activeClient.deactivate();
        }
        if (!options.silent) {
            this.callbacks.onStatus("disconnected");
        }
    }

    isConnected() {
        return Boolean(this.client?.connected);
    }

    handleConnect() {
        this.callbacks.onStatus("connected");
        this.callbacks.onError("");
        this.callbacks.onLog("connected", "Connected. Subscribing to chat and user queues.");
        this.subscriptions = [
            this.client.subscribe(this.config.destinations.chatSubscribe,
                (frame) => this.handleChatMessage(frame)),
            this.client.subscribe(this.config.destinations.healthSubscribe,
                (frame) => this.handleHealthCheck(frame)),
            this.client.subscribe(this.config.destinations.serverPushSubscribe,
                (frame) => this.handleServerPush(frame)),
        ];
        this.startHealthCheck();
        this.callbacks.onConnected();
    }

    handleChatMessage(frame) {
        try {
            this.callbacks.onChatMessage(JSON.parse(frame.body));
        } catch (error) {
            this.callbacks.onError("Chat payload is not valid JSON: " + error.message);
            this.callbacks.onLog("error", "Failed to parse chat response.", frame.body);
        }
    }

    handleServerPush(frame) {
        try {
            this.callbacks.onServerPush(JSON.parse(frame.body));
        } catch (error) {
            this.callbacks.onError("Server-push payload is not valid JSON: " + error.message);
            this.callbacks.onLog("error", "Failed to parse server-push response.", frame.body);
        }
    }

    startHealthCheck() {
        this.stopHealthCheck();
        this.callbacks.onHealth("checking");
        this.sendHealthCheck();
        this.healthTimerId = window.setInterval(() => this.sendHealthCheck(),
            this.config.healthIntervalMs);
    }

    stopHealthCheck() {
        if (this.healthTimerId) {
            window.clearInterval(this.healthTimerId);
            this.healthTimerId = null;
        }
        if (this.healthTimeoutId) {
            window.clearTimeout(this.healthTimeoutId);
            this.healthTimeoutId = null;
        }
        this.healthPending = null;
    }

    sendHealthCheck() {
        if (!this.client?.connected) {
            return;
        }
        if (this.healthPending) {
            this.handleHealthCheckTimeout(this.healthPending.sequence);
            return;
        }

        const clientTime = new Date().toISOString();
        const sequence = this.healthSequence + 1;
        this.healthSequence = sequence;
        this.healthPending = { sequence, sentAt: performance.now(), clientTime };
        this.callbacks.onHealth("checking");
        this.client.publish({
            destination: this.config.destinations.healthPublish,
            headers: { "content-type": "application/json" },
            body: JSON.stringify({ clientTime }),
        });
        this.healthTimeoutId = window.setTimeout(() => this.handleHealthCheckTimeout(sequence),
            this.config.healthTimeoutMs);
        this.callbacks.onLog("health", "Healthcheck ping sent.");
    }

    handleHealthCheck(frame) {
        const pending = this.healthPending;
        if (!pending) {
            this.callbacks.onLog("warn", "Received healthcheck pong without a pending ping.", frame.body);
            return;
        }

        let parsed;
        try {
            parsed = JSON.parse(frame.body);
        } catch (error) {
            this.callbacks.onError("Healthcheck payload is not valid JSON: " + error.message);
            this.callbacks.onLog("error", "Failed to parse healthcheck response.", frame.body);
            return;
        }

        if (parsed.status !== "pong" || parsed.clientTime !== pending.clientTime
            || typeof parsed.serverTime !== "string" || Number.isNaN(Date.parse(parsed.serverTime))) {
            this.callbacks.onError("Healthcheck response is invalid.");
            this.callbacks.onLog("warn", "Unexpected healthcheck response.", frame.body);
            return;
        }

        if (this.healthTimeoutId) {
            window.clearTimeout(this.healthTimeoutId);
            this.healthTimeoutId = null;
        }

        const rtt = Math.round(performance.now() - pending.sentAt) + " ms";
        this.healthPending = null;
        this.callbacks.onHealth("pong " + rtt);
        this.callbacks.onConnectionIdentity?.({
            nodeId: parsed.nodeId || "-",
            sessionId: parsed.sessionId || "-",
        });
        this.callbacks.onError("");
        this.callbacks.onLog("health", "Healthcheck pong received in " + rtt);
    }

    handleHealthCheckTimeout(sequence) {
        if (!this.healthPending || this.healthPending.sequence !== sequence) {
            return;
        }
        const message = "Healthcheck timed out after " + this.config.healthTimeoutMs + " ms.";
        this.stopHealthCheck();
        this.callbacks.onHealth("timeout");
        this.callbacks.onError(message);
        this.callbacks.onStatus("error");
        this.callbacks.onLog("error", message);
        const staleClient = this.client;
        this.client = null;
        if (staleClient) {
            void staleClient.deactivate();
        }
    }

    handleStompError(frame) {
        const message = frame.headers.message || "Broker returned STOMP error.";
        this.callbacks.onStatus("error");
        this.callbacks.onError(message);
        this.callbacks.onLog("error", message, frame.body || "");
    }

    handleWebSocketError() {
        const message = "WebSocket error. Check server status, URL, and token.";
        this.callbacks.onStatus("error");
        this.callbacks.onError(message);
        this.callbacks.onLog("error", message);
    }

    handleWebSocketClose(event) {
        const reason = event.reason ? ": " + event.reason : "";
        this.stopHealthCheck();
        if (this.callbacks.currentStatus() !== "disconnecting" && this.callbacks.currentStatus() !== "error") {
            this.callbacks.onStatus("disconnected");
        }
        this.callbacks.onLog("closed", "WebSocket closed with code " + event.code + reason);
    }

    handleDisconnect() {
        this.stopHealthCheck();
        if (this.callbacks.currentStatus() !== "error") {
            this.callbacks.onStatus("disconnected");
        }
        this.callbacks.onLog("disconnected", "STOMP session disconnected.");
    }

    assertStompLoaded() {
        if (!window.StompJs || !window.StompJs.Client) {
            throw new Error("STOMP client library was not loaded. Check CDN access.");
        }
    }

    validateSocketUrl(rawValue) {
        const value = rawValue.trim();
        if (!value) {
            throw new Error("WebSocket URL is required.");
        }
        const url = new URL(value);
        if (url.protocol !== "ws:" && url.protocol !== "wss:") {
            throw new Error("WebSocket URL must start with ws:// or wss://.");
        }
        return url.toString();
    }
}