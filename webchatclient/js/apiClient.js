export class ApiError extends Error {
    constructor(message, details = {}) {
        super(message);
        this.name = "ApiError";
        this.status = details.status ?? null;
        this.path = details.path ?? null;
        this.payload = details.payload ?? null;
    }
}

export function makeRequestId() {
    return Date.now() * 1000 + Math.floor(Math.random() * 1000);
}

export function normalizeBaseUrl(rawValue) {
    const value = rawValue.trim();
    if (!value) {
        throw new Error("Base URL is required.");
    }
    const url = new URL(value);
    if (url.protocol !== "http:" && url.protocol !== "https:") {
        throw new Error("Base URL must start with http:// or https://.");
    }
    return url.toString().replace(/\/$/, "");
}

async function requestJson(baseUrl, path, options = {}) {
    const url = new URL(path, normalizeBaseUrl(baseUrl) + "/");
    let response;
    try {
        response = await fetch(url, {
            ...options,
            headers: {
                Accept: "application/json",
                ...(options.body ? { "Content-Type": "application/json" } : {}),
                ...(options.headers ?? {}),
            },
        });
    } catch (error) {
        throw new ApiError("API server is unreachable.", { payload: error.message });
    }

    const text = await response.text();
    const payload = text ? parseJson(text) : null;
    if (!response.ok) {
        const message = payload?.message || `${response.status} ${response.statusText}`;
        throw new ApiError(message, { status: response.status, path: payload?.path, payload });
    }
    return payload;
}

function parseJson(text) {
    try {
        return JSON.parse(text);
    } catch (error) {
        throw new ApiError("API returned invalid JSON.", { payload: text });
    }
}

export function login(apiBaseUrl, { username, password }) {
    return requestJson(apiBaseUrl, "/api/auth/login", {
        method: "POST",
        body: JSON.stringify({ requestId: makeRequestId(), username, password }),
    });
}

export function register(apiBaseUrl, { username, password }) {
    return requestJson(apiBaseUrl, "/api/auth/register", {
        method: "POST",
        body: JSON.stringify({ requestId: makeRequestId(), username, password }),
    });
}

export function sendChatMessage(apiBaseUrl, session, message) {
    return requestJson(apiBaseUrl, "/api/messages", {
        method: "POST",
        headers: {
            Authorization: `${session.tokenType || "Bearer"} ${session.accessToken}`,
        },
        body: JSON.stringify({
            requestId: makeRequestId(),
            message,
            clientTime: new Date().toISOString(),
        }),
    });
}

export function fetchOnlineCount(apiBaseUrl) {
    return requestJson(apiBaseUrl, "/api/online-count?requestId=" + makeRequestId());
}

export function fetchChatEvents(apiBaseUrl, session, limit = 100) {
    return requestJson(apiBaseUrl, "/api/chat/events?requestId=" + makeRequestId()
        + "&limit=" + encodeURIComponent(String(limit)), {
        headers: {
            Authorization: `${session.tokenType || "Bearer"} ${session.accessToken}`,
        },
    });
}

export function fetchChatMembers(apiBaseUrl, session) {
    return requestJson(apiBaseUrl, "/api/chat/members?requestId=" + makeRequestId(), {
        headers: {
            Authorization: `${session.tokenType || "Bearer"} ${session.accessToken}`,
        },
    });
}