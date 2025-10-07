package com.finance.tracker.transactions.domain;

public record SearchRequest(
        String fromDate,
        String toDate,
        String account,
        String category,
        String query
) {
}
