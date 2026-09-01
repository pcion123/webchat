export function createInitialState(config) {
    return {
        config: {
            apiBaseUrl: config.apiBaseUrl,
            socketUrl: config.socketUrl,
        },
        authMode: "login",
        auth: {
            status: "anonymous",
            session: null,
            message: "",
        },
        socket: {
            status: "idle",
            lastHealth: "-",
            nodeId: "-",
            sessionId: "-",
        },
        chat: {
            messages: [],
            messagesVersion: 0,
            eventIds: new Set(),
            members: [],
            onlineCount: "-",
            sessionCount: "-",
            historyLoaded: false,
        },
        ui: {
            diagnosticsCollapsed: true,
            theme: "light",
        },
        logs: [],
    };
}

export function addLog(state, kind, message, payload, maxLogs) {
    state.logs.unshift({
        kind,
        message,
        payload: formatPayload(payload),
        time: formatTime(new Date()),
    });
    if (state.logs.length > maxLogs) {
        state.logs.length = maxLogs;
    }
}

export function addMessage(state, message, maxMessages) {
    if (message.eventId && state.chat.eventIds.has(message.eventId)) {
        return false;
    }
    const entry = {
        ...message,
        localId: message.localId || crypto.randomUUID(),
        timestamp: message.timestamp || new Date().toISOString(),
    };
    if (entry.eventId) {
        state.chat.eventIds.add(entry.eventId);
    }
    state.chat.messages.push(entry);
    if (state.chat.messages.length > maxMessages) {
        const removed = state.chat.messages.splice(0, state.chat.messages.length - maxMessages);
        removed.forEach((item) => {
            if (item.eventId) {
                state.chat.eventIds.delete(item.eventId);
            }
        });
    }
    state.chat.messagesVersion += 1;
    return true;
}

export function clearMessages(state) {
    state.chat.messages = [];
    state.chat.eventIds.clear();
    state.chat.messagesVersion += 1;
}

export function formatTime(date) {
    return new Intl.DateTimeFormat("en", {
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        fractionalSecondDigits: 3,
        hour12: false,
    }).format(date);
}

function formatPayload(payload) {
    if (!payload) {
        return "";
    }
    if (typeof payload === "string") {
        return payload;
    }
    return JSON.stringify(payload, null, 2);
}