package com.finance.tracker.auth.domain;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        UserType userType,
        DashboardMode dashboardMode
) {
}
