package com.springtest.webchatapi.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserModel {

    private String userId;
    private String username;
    private Integer level;
    private BigDecimal money;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}