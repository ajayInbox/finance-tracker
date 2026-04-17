package com.finance.tracker.sync.controller;

import com.finance.tracker.sync.domain.EndScanRequest;
import com.finance.tracker.sync.domain.dtos.ScanResponse;
import com.finance.tracker.sync.domain.dtos.ScanStartResponse;
import com.finance.tracker.sync.domain.dtos.SyncMetadataResponse;
import com.finance.tracker.sync.service.SyncService;
import com.finance.tracker.transactions.domain.BatchSyncRequest;
import com.finance.tracker.transactions.domain.BatchSyncResponse;
import com.finance.tracker.transactions.service.TransactionBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final TransactionBatchService batchService;

    @GetMapping("/latest-timestamp")
    public ResponseEntity<SyncMetadataResponse> getMetadata(
            @RequestHeader("X-User-Id") UUID userId) {

        SyncMetadataResponse response = syncService.getMetadata(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<ScanStartResponse> startScan(
            @RequestHeader("X-User-Id") UUID userId) {

        ScanStartResponse response = syncService.startScan(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/finalize-end")
    public ResponseEntity<ScanResponse> finalizeScan(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam("scanId") UUID scanId,
            @RequestBody EndScanRequest endScanRequest) {

        ScanResponse response = syncService.finalizeScan(userId, scanId, endScanRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<ScanResponse> endScan(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam("scanId") UUID scanId) {

        ScanResponse response = syncService.endScan(userId, scanId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/latest-timestamp")
    public ResponseEntity<SyncMetadataResponse> updateMetadata(
            @RequestHeader("X-User-Id") UUID userId, @RequestParam("timestamp") long timestamp) {

        SyncMetadataResponse response = syncService.updateMetadata(userId,timestamp);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // Parse SMS Messages in Batch
    // -----------------------------------------------------

    @PostMapping("/batch-upload")
    public ResponseEntity<BatchSyncResponse> uploadBatch(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestBody BatchSyncRequest request) {
        BatchSyncResponse response = batchService.processBatch(userId, request);
        return ResponseEntity.ok(response);
    }
}
