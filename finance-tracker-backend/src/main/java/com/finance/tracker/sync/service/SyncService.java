package com.finance.tracker.sync.service;

import com.finance.tracker.sync.domain.EndScanRequest;
import com.finance.tracker.sync.domain.dtos.ScanResponse;
import com.finance.tracker.sync.domain.dtos.ScanStartResponse;
import com.finance.tracker.sync.domain.dtos.SyncMetadataResponse;

import java.util.UUID;

public interface SyncService {
    public SyncMetadataResponse getMetadata(UUID userId);
    ScanStartResponse startScan(UUID userId);

    ScanResponse finalizeScan(UUID userId, UUID scanId, EndScanRequest endScanRequest);

    SyncMetadataResponse updateMetadata(UUID userId, long timestamp);

    ScanResponse endScan(UUID userId, UUID scanId);
}