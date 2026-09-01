package com.springtest.webchatapi.model.dto.api;

import java.util.List;

public record OnlineSessionsResponse(int onlineCount, List<OnlineSessionResponse> sessions) {
}
