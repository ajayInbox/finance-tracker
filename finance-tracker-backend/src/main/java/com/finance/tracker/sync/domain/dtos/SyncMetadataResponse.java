package com.finance.tracker.sync.domain.dtos;

import java.util.UUID;

public record SyncMetadataResponse(
    UUID userId,
    long latestScannedTimestamp,
    String status
) {}