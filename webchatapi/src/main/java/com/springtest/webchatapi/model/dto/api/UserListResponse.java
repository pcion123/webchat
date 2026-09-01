package com.springtest.webchatapi.model.dto.api;

import java.util.List;

public record UserListResponse(List<UserResponse> items, int page, int size, long total) {
}
