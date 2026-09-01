package com.springtest.webchatapi.model.dto.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.springtest.webchatapi.model.entity.UserModel;

public record UserResponse(String userId, String username, Integer level, BigDecimal money,
        LocalDateTime createTime, LocalDateTime updateTime) {

    public static UserResponse from(UserModel userModel) {
        return new UserResponse(userModel.getUserId(), userModel.getUsername(),
                userModel.getLevel(), userModel.getMoney(), userModel.getCreateTime(),
                userModel.getUpdateTime());
    }
}
