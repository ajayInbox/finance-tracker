package com.finance.tracker.transactions.domain;

public record BatchSyncResponse(
        int newCount,
        int duplicateCount,
        int failedToParse
) {
}
