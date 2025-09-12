package com.finance.tracker.category.domain;

public record CategoryCreateUpdateRequest(
        String label,
        String categoryType
) {
}
