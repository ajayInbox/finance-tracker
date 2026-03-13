package com.finance.tracker.transactions.domain;

import java.time.LocalDateTime;

public record ExpenseReportRequest(
        LocalDateTime start,
        LocalDateTime end,
        String type
) {
}
