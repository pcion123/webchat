package com.springtest.webchatapi.model.entity;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Account {

    private String accountId;
    private String userId;
    private String username;
    private String passwordHash;
    private String status;
    private LocalDateTime lastLoginTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}