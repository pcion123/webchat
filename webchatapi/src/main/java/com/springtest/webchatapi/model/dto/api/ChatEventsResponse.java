package com.springtest.webchatapi.model.dto.api;

import java.util.List;

public record ChatEventsResponse(String roomId, int limit, List<ChatEventResponse> events) {
}
