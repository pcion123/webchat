import { defaultConfig } from "./config.js";
import { fetchChatEvents, fetchChatMembers, login, normalizeBaseUrl, register, sendChatMessage } from "./apiClient.js";
import { clearSession, loadSession, saveSession, sessionFromLogin } from "./authStore.js";
import { addLog, addMessage, clearMessages, createInitialState } from "./chatStore.js";
import { WebchatStompClient } from "./stompClient.js";
import { initUi } from "./ui.js";

const state = createInitialState(defaultConfig);
let ui;
let stompClient;

function loadThemePreference() {
    const stored = localStorage.getItem(defaultConfig.themeStorageKey);
    if (stored === "dark" || stored === "light") {
        return stored;
    }
    return window.matchMedia?.("(prefers-color-scheme: dark)").matches ? "dark" : "light";
}

function render() {
    ui.render(state);
}

function log(kind, message, payload) {
    addLog(state, kind, message, payload, defaultConfig.maxLogs);
    render();
}

function setSocketStatus(status) {
    state.socket.status = status;
    if (status !== "connected") {
        clearConnectionIdentity();
    }
    render();
}

function setHealth(value) {
    state.socket.lastHealth = value;
    render();
}

function setConnectionIdentity(identity) {
    const nodeId = normalizeIdentityValue(identity?.nodeId);
    const sessionId = normalizeIdentityValue(identity?.sessionId);
    const changed = state.socket.nodeId !== nodeId || state.socket.sessionId !== sessionId;
    state.socket.nodeId = nodeId;
    state.socket.sessionId = sessionId;
    if (changed && (nodeId !== "-" || sessionId !== "-")) {
        log("connected", "Connected to WebSocket node " + nodeId
            + " with session " + shortSessionId(sessionId) + ".", { nodeId, sessionId });
        return;
    }
    if (changed) {
        render();
    }
}

function clearConnectionIdentity() {
    state.socket.nodeId = "-";
    state.socket.sessionId = "-";
}

async function handleAuthSubmit(form) {
    state.auth.message = "";
    if (!form.username || !form.password) {
        state.auth.message = "Username and password are required.";
        render();
        return;
    }
    if (state.authMode === "register" && form.password !== form.confirmPassword) {
        state.auth.message = "Passwords do not match.";
        render();
        return;
    }

    try {
        state.auth.status = "authenticating";
        render();
        const apiBaseUrl = normalizeBaseUrl(ui.readSettings().apiBaseUrl);
        state.config.apiBaseUrl = apiBaseUrl;
        if (state.authMode === "register") {
            await register(apiBaseUrl, form);
            log("auth", "Account created. Logging in as " + form.username + ".");
        }
        const response = await login(apiBaseUrl, form);
        const session = sessionFromLogin(response);
        saveSession(defaultConfig.storageKey, session);
        state.auth.session = session;
        state.auth.status = "authenticated";
        state.auth.message = "";
        addMessage(state, {
            kind: "system",
            author: "System",
            text: "Signed in as " + session.username + ".",
        }, defaultConfig.maxMessages);
        log("auth", "Signed in as " + session.username + ".");
        render();
        connectSocket(ui.readSettings());
    } catch (error) {
        state.auth.status = "anonymous";
        state.auth.message = error.message;
        log("error", error.message, error.payload ? sanitizePayload(error.payload) : "");
        render();
    }
}

function handleAuthModeChange(mode) {
    state.authMode = mode;
    state.auth.message = "";
    render();
}

async function handleLogout() {
    await stompClient.disconnect({ silent: false });
    clearSession(defaultConfig.storageKey);
    state.auth.status = "anonymous";
    state.auth.session = null;
    state.socket.status = "idle";
    state.socket.lastHealth = "-";
    clearConnectionIdentity();
    state.chat.onlineCount = "-";
    state.chat.sessionCount = "-";
    state.chat.members = [];
    state.chat.historyLoaded = false;
    addMessage(state, { kind: "system", author: "System", text: "Logged out." },
        defaultConfig.maxMessages);
    log("auth", "Logged out.");
    render();
}

function handleConnect(settings) {
    connectSocket(settings);
}

function connectSocket(settings) {
    if (!state.auth.session) {
        log("warn", "Login before connecting to the room.");
        return;
    }
    try {
        state.config.apiBaseUrl = normalizeBaseUrl(settings.apiBaseUrl);
        state.config.socketUrl = settings.socketUrl;
        stompClient.connect(state.config.socketUrl, state.auth.session);
    } catch (error) {
        setSocketStatus("error");
        log("error", error.message);
    }
}

async function handleDisconnect() {
    await stompClient.disconnect({ silent: false });
    state.socket.lastHealth = "-";
    clearConnectionIdentity();
    state.chat.onlineCount = "-";
    render();
}

async function handleSendMessage(rawMessage) {
    const message = rawMessage.trim();
    if (!message) {
        log("warn", "Message was not sent because it is empty.");
        return;
    }
    if (!state.auth.session) {
        log("warn", "Message was not sent because there is no active session.");
        return;
    }

    try {
        const response = await sendChatMessage(state.config.apiBaseUrl, state.auth.session, message);
        ui.clearMessageInput();
        log("accepted", "Message accepted by API.", response);
    } catch (error) {
        addMessage(state, { kind: "error", author: "System", text: error.message },
            defaultConfig.maxMessages);
        log("error", error.message);
    }
}

function handleChatMessage(payload) {
    handleChatEvent(payload);
    render();
}

function handleServerPush(payload) {
    const eventType = payload.eventType || "server-push";
    if (isRoomEventType(eventType)) {
        handleChatEvent(payload);
        return;
    }
    addMessage(state, {
        kind: "system",
        author: "Server push",
        text: eventType,
        timestamp: payload.timestamp,
    }, defaultConfig.maxMessages);
    log("received", "Server push received: " + eventType, sanitizePayload(payload));
    render();
}

function handleClearMessages() {
    clearMessages(state);
    render();
}

function handleClearLogs() {
    state.logs = [];
    render();
}

function handleToggleDiagnostics() {
    state.ui.diagnosticsCollapsed = !state.ui.diagnosticsCollapsed;
    render();
}

function handleToggleTheme() {
    state.ui.theme = state.ui.theme === "dark" ? "light" : "dark";
    localStorage.setItem(defaultConfig.themeStorageKey, state.ui.theme);
    render();
}

function loadInitialRoomSnapshot() {
    void loadChatSnapshot();
}

async function loadChatSnapshot() {
    await Promise.all([loadChatHistory(), pollChatMembers()]);
}

async function loadChatHistory() {
    if (!state.auth.session) {
        return;
    }
    try {
        const response = await fetchChatEvents(state.config.apiBaseUrl, state.auth.session, 100);
        clearMessages(state);
        (response.events || []).forEach((event) => addEventMessage(event, { logEvent: false }));
        state.chat.historyLoaded = true;
        render();
        log("received", "Loaded recent room history.");
    } catch (error) {
        state.chat.historyLoaded = false;
        log("warn", "Room history unavailable: " + error.message);
    }
}

async function pollChatMembers() {
    if (!state.auth.session) {
        return;
    }
    try {
        const response = await fetchChatMembers(state.config.apiBaseUrl, state.auth.session);
        state.chat.onlineCount = String(response.onlineCount ?? response.count ?? "-");
        state.chat.sessionCount = String(response.sessionCount ?? response.onlineCount ?? "-");
        state.chat.members = response.members || [];
        render();
    } catch (error) {
        state.chat.onlineCount = "-";
        state.chat.sessionCount = "-";
        state.chat.members = [];
        log("warn", "Member list unavailable: " + error.message);
    }
}

function handleChatEvent(payload, options = {}) {
    const event = normalizeRoomEvent(payload);
    if (!isRoomEventType(event.eventType)) {
        return false;
    }
    const added = addEventMessage(event, options);
    if (added && options.logEvent !== false) {
        logRoomEvent(event);
    }
    if (event.eventType === "chat-user-joined" || event.eventType === "chat-user-left") {
        void pollChatMembers();
    }
    render();
    return added;
}

function addEventMessage(event, options = {}) {
    const session = state.auth.session;
    const userId = event.userId || "";
    const username = event.username || userId || "Unknown";
    if (event.eventType === "chat-message-created") {
        const kind = session && userId === session.userId ? "mine" : "others";
        return addMessage(state, {
            eventId: event.eventId,
            kind,
            author: username,
            text: event.message || "",
            timestamp: event.occurredAt,
        }, defaultConfig.maxMessages);
    }
    const action = event.eventType === "chat-user-joined" ? "joined" : "left";
    return addMessage(state, {
        eventId: event.eventId,
        kind: "system",
        author: "System",
        text: username + " " + action + " the room.",
        timestamp: event.occurredAt,
    }, defaultConfig.maxMessages);
}

function normalizeRoomEvent(raw) {
    const payload = raw?.payload || {};
    const eventType = raw?.eventType || payload.eventType || "";
    return {
        eventId: raw?.eventId || payload.eventId || null,
        eventType,
        userId: raw?.userId || payload.senderUserId || payload.userId || "",
        username: raw?.username || payload.senderUsername || payload.username || "",
        message: raw?.message ?? payload.message ?? "",
        occurredAt: raw?.occurredAt || payload.timestamp || raw?.timestamp || new Date().toISOString(),
    };
}

function isRoomEventType(eventType) {
    return eventType === "chat-message-created" || eventType === "chat-user-joined"
        || eventType === "chat-user-left";
}

function logRoomEvent(event) {
    if (event.eventType === "chat-message-created") {
        log("chat", "Received chat message from " + (event.username || event.userId || "unknown") + ".");
        return;
    }
    log("received", "Room event received: " + event.eventType, sanitizePayload(event));
}

function sanitizePayload(payload) {
    if (!payload || typeof payload !== "object") {
        return payload;
    }
    const clone = { ...payload };
    delete clone.accessToken;
    delete clone.password;
    return clone;
}

function normalizeIdentityValue(value) {
    return typeof value === "string" && value.trim() ? value.trim() : "-";
}

function shortSessionId(sessionId) {
    return sessionId === "-" ? sessionId : sessionId.slice(0, 12);
}

function restoreSession() {
    const session = loadSession(defaultConfig.storageKey);
    if (!session) {
        log("idle", "Ready. Login through webchatapi to connect to the public room.");
        return;
    }
    state.auth.session = session;
    state.auth.status = "authenticated";
    addMessage(state, {
        kind: "system",
        author: "System",
        text: "Restored session for " + session.username + ".",
    }, defaultConfig.maxMessages);
    log("auth", "Restored session for " + session.username + ".");
    render();
    connectSocket(ui.readSettings());
}

function bootstrap() {
    ui = initUi({
        onAuthModeChange: handleAuthModeChange,
        onAuthSubmit: handleAuthSubmit,
        onLogout: handleLogout,
        onConnect: handleConnect,
        onDisconnect: handleDisconnect,
        onSendMessage: handleSendMessage,
        onClearMessages: handleClearMessages,
        onClearLogs: handleClearLogs,
        onToggleDiagnostics: handleToggleDiagnostics,
        onToggleTheme: handleToggleTheme,
    });
    ui.setConfig(defaultConfig);
    state.ui.theme = loadThemePreference();
    stompClient = new WebchatStompClient(defaultConfig, {
        currentStatus: () => state.socket.status,
        onStatus: setSocketStatus,
        onError: () => { },
        onHealth: setHealth,
        onConnectionIdentity: setConnectionIdentity,
        onLog: log,
        onChatMessage: handleChatMessage,
        onServerPush: handleServerPush,
        onConnected: loadInitialRoomSnapshot,
    });
    render();
    restoreSession();
}

bootstrap();