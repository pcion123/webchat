package com.springtest.webchatapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.springtest.webchatapi.model.dto.api.CreateUserRequest;
import com.springtest.webchatapi.model.dto.api.UpdateUserRequest;
import com.springtest.webchatapi.model.dto.api.UserListResponse;
import com.springtest.webchatapi.model.dto.api.UserResponse;
import com.springtest.webchatapi.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

@Validated
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @GetMapping("/{userId}")
    public UserResponse getUser(@RequestParam long requestId,
            @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{32}$") String userId) {
        return userService.getUser(userId);
    }

    @GetMapping
    public UserListResponse listUsers(@RequestParam long requestId,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return userService.listUsers(username, page, size);
    }

    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{32}$") String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@RequestParam long requestId,
            @PathVariable @Pattern(regexp = "^[0-9a-fA-F]{32}$") String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
