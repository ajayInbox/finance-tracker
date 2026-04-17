package com.finance.tracker.sync.domain.dtos;

import com.finance.tracker.sync.domain.ScanStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScanResponse(
        UUID id,
        ScanStatus status,
        OffsetDateTime startTime,
        OffsetDateTime endTime,
        int totalSmsProcessed,
        int transactionsCreated,
        int duplicatesSkipped,
        int failedToParse
) {
}
