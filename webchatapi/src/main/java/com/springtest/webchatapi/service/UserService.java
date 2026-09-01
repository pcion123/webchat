package com.springtest.webchatapi.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.github.f4b6a3.uuid.UuidCreator;
import com.springtest.webchatapi.exception.UserNotFoundException;
import com.springtest.webchatapi.model.dto.api.CreateUserRequest;
import com.springtest.webchatapi.model.dto.api.UpdateUserRequest;
import com.springtest.webchatapi.model.dto.api.UserListResponse;
import com.springtest.webchatapi.model.dto.api.UserResponse;
import com.springtest.webchatapi.model.entity.UserModel;
import com.springtest.webchatapi.repository.UserMapper;

@Service
public class UserService {

    private static final Integer DEFAULT_LEVEL = 0;
    private static final BigDecimal DEFAULT_MONEY = new BigDecimal("0.000");

    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        UserModel userModel = new UserModel();
        userModel.setUserId(UuidCreator.getTimeOrderedEpoch().toString().replace("-", ""));
        userModel.setUsername(request.getUsername().trim());
        userModel.setLevel(request.getLevel() == null ? DEFAULT_LEVEL : request.getLevel());
        userModel.setMoney(request.getMoney() == null ? DEFAULT_MONEY : request.getMoney());

        userMapper.insert(userModel);
        return userMapper.findById(userModel.getUserId()).map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(userModel.getUserId()));
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(String userId) {
        return userMapper.findById(userId).map(UserResponse::from)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional(readOnly = true)
    public UserListResponse listUsers(String username, int page, int size) {
        String normalizedUsername = normalizeUsername(username);
        int offset = page * size;
        List<UserResponse> items = userMapper.findAll(normalizedUsername, size, offset).stream()
                .map(UserResponse::from).toList();
        long total = userMapper.count(normalizedUsername);
        return new UserListResponse(items, page, size, total);
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        userMapper.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserModel userModel = new UserModel();
        userModel.setUserId(userId);
        userModel.setUsername(request.getUsername().trim());
        userModel.setLevel(request.getLevel());
        userModel.setMoney(request.getMoney());

        if (userMapper.update(userModel) == 0) {
            throw new UserNotFoundException(userId);
        }
        return getUser(userId);
    }

    @Transactional
    public void deleteUser(String userId) {
        if (userMapper.deleteById(userId) == 0) {
            throw new UserNotFoundException(userId);
        }
    }

    private String normalizeUsername(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return username.trim();
    }
}
