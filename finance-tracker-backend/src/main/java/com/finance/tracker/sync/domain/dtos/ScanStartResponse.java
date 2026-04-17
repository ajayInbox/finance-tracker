package com.finance.tracker.sync.domain.dtos;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ScanStartResponse(
    UUID scanId,
    String status,
    OffsetDateTime startTime
) {}