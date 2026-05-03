package com.finance.tracker.auth.domain;

import jakarta.validation.constraints.*;

public record RegisterRequest (
        @NotNull(message = "Email is required")
        @Email(message = "Invalid email format")
        @NotBlank(message = "Email is required")
        String email,
        @NotBlank
        @Size(min = 8, max = 18, message = "Password length should be between 8 to 18")
        String password,
        @NotBlank
        String name
) { }