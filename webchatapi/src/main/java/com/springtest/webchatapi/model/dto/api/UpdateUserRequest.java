package com.springtest.webchatapi.model.dto.api;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    @NotNull
    private Long requestId;

    @NotBlank
    @Size(max = 64)
    private String username;

    @NotNull
    @Min(0)
    private Integer level;

    @NotNull
    @DecimalMin("0.000")
    @Digits(integer = 16, fraction = 3)
    private BigDecimal money;
}
