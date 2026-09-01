export function sessionFromLogin(response) {
    if (!response?.accessToken || !response?.userId || !response?.username || !response?.expiresAt) {
        throw new Error("Login response is missing required session fields.");
    }
    return {
        userId: response.userId,
        username: response.username,
        accessToken: response.accessToken,
        tokenType: response.tokenType || "Bearer",
        expiresAt: response.expiresAt,
    };
}

export function saveSession(storageKey, session) {
    sessionStorage.setItem(storageKey, JSON.stringify(session));
}

export function clearSession(storageKey) {
    sessionStorage.removeItem(storageKey);
}

export function loadSession(storageKey) {
    const rawValue = sessionStorage.getItem(storageKey);
    if (!rawValue) {
        return null;
    }

    try {
        const session = JSON.parse(rawValue);
        if (!isValidSession(session) || isExpired(session)) {
            clearSession(storageKey);
            return null;
        }
        return session;
    } catch (error) {
        clearSession(storageKey);
        return null;
    }
}

export function isExpired(session) {
    const expiresAt = Date.parse(session?.expiresAt ?? "");
    return Number.isNaN(expiresAt) || expiresAt <= Date.now();
}

function isValidSession(session) {
    return Boolean(session?.userId && session?.username && session?.accessToken && session?.expiresAt);
}