package com.finance.tracker.auth.domain;

public record LoginRequest(
        String email,
        String password
) {
}
