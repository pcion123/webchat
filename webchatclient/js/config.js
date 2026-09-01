export const defaultConfig = {
    apiBaseUrl: "http://localhost:9091",
    socketUrl: "ws://localhost:9092/ws",
    storageKey: "webchatclient.session.v1",
    themeStorageKey: "webchatclient.theme.v1",
    destinations: {
        chatSubscribe: "/topic/chat.public",
        healthPublish: "/app/healthcheck",
        healthSubscribe: "/user/queue/healthcheck",
        serverPushSubscribe: "/user/queue/server-push",
    },
    healthIntervalMs: 15000,
    healthTimeoutMs: 5000,
    maxLogs: 80,
    maxMessages: 160,
};