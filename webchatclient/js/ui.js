export function initUi(handlers) {
    const elements = getElements();

    elements.loginModeButton.addEventListener("click", () => handlers.onAuthModeChange("login"));
    elements.registerModeButton.addEventListener("click", () => handlers.onAuthModeChange("register"));
    elements.authForm.addEventListener("submit", (event) => {
        event.preventDefault();
        handlers.onAuthSubmit(readAuthForm(elements));
    });
    elements.logoutButton.addEventListener("click", handlers.onLogout);
    elements.connectButton.addEventListener("click", () => handlers.onConnect(readSettings(elements)));
    elements.disconnectButton.addEventListener("click", handlers.onDisconnect);
    elements.messageForm.addEventListener("submit", (event) => {
        event.preventDefault();
        handlers.onSendMessage(elements.messageInput.value);
    });
    elements.messageInput.addEventListener("keydown", (event) => {
        if (event.key === "Enter" && !event.shiftKey && !event.isComposing) {
            event.preventDefault();
            handlers.onSendMessage(elements.messageInput.value);
        }
    });
    elements.clearMessagesButton.addEventListener("click", handlers.onClearMessages);
    elements.clearLogButton.addEventListener("click", handlers.onClearLogs);
    elements.diagnosticsToggleButton.addEventListener("click", handlers.onToggleDiagnostics);
    elements.themeToggleButton.addEventListener("click", handlers.onToggleTheme);

    return {
        render: (state) => render(elements, state),
        clearMessageInput: () => {
            elements.messageInput.value = "";
        },
        readSettings: () => readSettings(elements),
        setConfig: (config) => {
            elements.apiBaseUrl.value = config.apiBaseUrl;
            elements.socketUrl.value = config.socketUrl;
        },
    };
}

function getElements() {
    const ids = [
        "statusCard", "statusText", "authPanel", "sessionPanel", "loginModeButton",
        "registerModeButton", "authForm", "usernameInput", "passwordInput",
        "confirmPasswordField", "confirmPasswordInput", "authSubmitButton", "authMessage",
        "sessionAvatar", "sessionUsername", "sessionUserId", "sessionExpiresAt", "logoutButton",
        "apiBaseUrl", "socketUrl", "connectButton", "disconnectButton", "themeToggleButton",
        "onlineCount", "lastHealth", "socketNodeId", "socketSessionId", "messageStream", "messageForm", "messageInput",
        "sendButton", "clearMessagesButton", "memberSummary", "memberList",
        "diagnosticsPanel", "diagnosticsToggleButton", "logList", "clearLogButton",
    ];
    return Object.fromEntries(ids.map((id) => [id, document.getElementById(id)]));
}

function render(elements, state) {
    renderTheme(elements, state);
    renderAuthMode(elements, state);
    renderSession(elements, state);
    renderSocket(elements, state);
    renderMembers(elements, state);
    renderDiagnostics(elements, state);
    renderMessages(elements, state);
    renderLogs(elements, state);
}

function renderTheme(elements, state) {
    const theme = state.ui?.theme === "dark" ? "dark" : "light";
    document.documentElement.dataset.theme = theme;
    elements.themeToggleButton.textContent = theme === "dark" ? "Light" : "Dark";
    elements.themeToggleButton.setAttribute("aria-pressed", String(theme === "dark"));
}

function renderAuthMode(elements, state) {
    const isRegister = state.authMode === "register";
    elements.loginModeButton.classList.toggle("active", !isRegister);
    elements.registerModeButton.classList.toggle("active", isRegister);
    elements.confirmPasswordField.classList.toggle("hidden", !isRegister);
    elements.authSubmitButton.textContent = isRegister ? "Create account" : "Login";
    elements.authMessage.textContent = state.auth.message || "";
    elements.usernameInput.disabled = state.auth.status === "authenticating";
    elements.passwordInput.disabled = state.auth.status === "authenticating";
    elements.confirmPasswordInput.disabled = state.auth.status === "authenticating";
    elements.authSubmitButton.disabled = state.auth.status === "authenticating";
}

function renderSession(elements, state) {
    const session = state.auth.session;
    const isAuthenticated = Boolean(session);
    elements.authPanel.classList.toggle("hidden", isAuthenticated);
    elements.sessionPanel.classList.toggle("hidden", !isAuthenticated);
    if (!isAuthenticated) {
        return;
    }
    elements.sessionAvatar.textContent = session.username.slice(0, 1);
    elements.sessionUsername.textContent = session.username;
    elements.sessionUserId.textContent = session.userId;
    elements.sessionExpiresAt.textContent = "Expires " + formatDateTime(session.expiresAt);
}

function renderSocket(elements, state) {
    const status = state.socket.status;
    elements.statusText.textContent = status.charAt(0).toUpperCase() + status.slice(1);
    elements.statusCard.className = "status-card status-" + status;

    const isAuthenticated = Boolean(state.auth.session);
    const isConnected = status === "connected";
    const isBusy = status === "connecting" || status === "disconnecting";
    elements.connectButton.disabled = !isAuthenticated || isConnected || isBusy;
    elements.disconnectButton.disabled = !isConnected && status !== "connecting";
    elements.apiBaseUrl.disabled = isBusy || isConnected;
    elements.socketUrl.disabled = isBusy || isConnected;
    elements.messageInput.disabled = !isConnected;
    elements.sendButton.disabled = !isConnected;
    elements.onlineCount.textContent = state.chat.onlineCount;
    elements.lastHealth.textContent = formatHealthLatency(state.socket.lastHealth);
    renderCompactIdentifier(elements.socketNodeId, state.socket.nodeId);
    renderCompactIdentifier(elements.socketSessionId, state.socket.sessionId);
}

function renderMembers(elements, state) {
    const members = state.chat.members || [];
    const memberCount = members.length;
    const sessionCount = state.chat.sessionCount || "-";
    elements.memberSummary.textContent = memberCount === 0 ? "No active members."
        : memberCount + " members, " + sessionCount + " sessions.";
    if (memberCount === 0) {
        elements.memberList.innerHTML = '<div class="member-empty">No active members.</div>';
        return;
    }

    const fragment = document.createDocumentFragment();
    members.forEach((member) => {
        const item = document.createElement("article");
        item.className = "member-item";

        const avatar = document.createElement("span");
        avatar.className = "avatar small-avatar";
        const displayName = member.username || member.userId || "?";
        avatar.textContent = displayName.slice(0, 1);

        const body = document.createElement("div");
        const name = document.createElement("p");
        name.className = "identity-name";
        name.textContent = displayName;
        const meta = document.createElement("p");
        meta.className = "identity-id";
        const sessions = Number(member.sessionCount || 0);
        meta.textContent = (member.userId || "unknown") + " · " + sessions + " session"
            + (sessions === 1 ? "" : "s") + " · since " + formatDateTime(member.connectedAt);
        body.append(name, meta);
        item.append(avatar, body);
        fragment.append(item);
    });
    elements.memberList.replaceChildren(fragment);
}

function renderDiagnostics(elements, state) {
    const collapsed = Boolean(state.ui?.diagnosticsCollapsed);
    elements.diagnosticsPanel.classList.toggle("diagnostics-collapsed", collapsed);
    elements.diagnosticsToggleButton.textContent = collapsed ? "Show" : "Hide";
    elements.diagnosticsToggleButton.setAttribute("aria-expanded", String(!collapsed));
}

function renderMessages(elements, state) {
    const messagesVersion = String(state.chat.messagesVersion ?? 0);
    if (elements.messageStream.dataset.messagesVersion === messagesVersion) {
        return;
    }
    elements.messageStream.dataset.messagesVersion = messagesVersion;

    if (state.chat.messages.length === 0) {
        elements.messageStream.innerHTML = '<div class="empty-state">Login and connect to start chatting.</div>';
        return;
    }

    const fragment = document.createDocumentFragment();
    state.chat.messages.forEach((message) => {
        const item = document.createElement("article");
        item.className = "message-item " + message.kind;

        const author = document.createElement("p");
        author.className = "message-author";
        author.textContent = message.author || "System";

        const text = document.createElement("p");
        text.className = "message-text";
        text.textContent = message.text;

        const meta = document.createElement("p");
        meta.className = "message-meta";
        meta.textContent = formatDateTime(message.timestamp);

        item.append(author, text, meta);
        fragment.append(item);
    });

    const atBottom = elements.messageStream.scrollTop + elements.messageStream.clientHeight
        >= elements.messageStream.scrollHeight - 24;
    elements.messageStream.replaceChildren(fragment);
    if (atBottom) {
        elements.messageStream.scrollTop = elements.messageStream.scrollHeight;
    }
}

function renderCompactIdentifier(element, value) {
    const normalizedValue = value || "-";
    element.textContent = compactIdentifier(normalizedValue);
    element.title = normalizedValue;
}

function compactIdentifier(value) {
    if (!value || value === "-") {
        return "-";
    }
    return value.length <= 18 ? value : value.slice(0, 8) + "..." + value.slice(-6);
}

function formatHealthLatency(value) {
    return String(value || "-").replace(/^pong\s+/i, "");
}

function renderLogs(elements, state) {
    if (state.logs.length === 0) {
        elements.logList.innerHTML = '<div class="log-empty">No events yet.</div>';
        return;
    }

    const fragment = document.createDocumentFragment();
    state.logs.forEach((entry) => {
        const item = document.createElement("article");
        item.className = "log-item " + entry.kind;

        const time = document.createElement("div");
        time.className = "log-time";
        time.textContent = entry.time;

        const body = document.createElement("div");
        const kind = document.createElement("p");
        kind.className = "log-kind";
        kind.textContent = entry.kind;
        const message = document.createElement("p");
        message.className = "log-message";
        message.textContent = entry.message;
        body.append(kind, message);

        if (entry.payload) {
            const raw = document.createElement("pre");
            raw.className = "raw-payload";
            raw.textContent = entry.payload;
            body.append(raw);
        }

        item.append(time, body);
        fragment.append(item);
    });

    elements.logList.replaceChildren(fragment);
}

function readAuthForm(elements) {
    return {
        username: elements.usernameInput.value.trim(),
        password: elements.passwordInput.value,
        confirmPassword: elements.confirmPasswordInput.value,
    };
}

function readSettings(elements) {
    return {
        apiBaseUrl: elements.apiBaseUrl.value.trim(),
        socketUrl: elements.socketUrl.value.trim(),
    };
}

function formatDateTime(value) {
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return "-";
    }
    return new Intl.DateTimeFormat("en", {
        month: "short",
        day: "2-digit",
        hour: "2-digit",
        minute: "2-digit",
        second: "2-digit",
        hour12: false,
    }).format(date);
}