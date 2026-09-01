package com.springtest.webchatapi.model.dto.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    @NotNull
    private Long requestId;

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotBlank
    private String password;
}
