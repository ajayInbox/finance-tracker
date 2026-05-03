package com.finance.tracker.auth.domain.dtos;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
