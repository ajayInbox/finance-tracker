package com.finance.tracker.auth.domain;

public record UserResponse(
        String name,
        String email,
        boolean isSuscribed
) {
}
