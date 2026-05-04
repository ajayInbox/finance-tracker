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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sync")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;
    private final TransactionBatchService batchService;

    @GetMapping("/latest-timestamp")
    public ResponseEntity<SyncMetadataResponse> getMetadata(Authentication auth) {
        String userId = (String) auth.getPrincipal();
        SyncMetadataResponse response = syncService.getMetadata(UUID.fromString(userId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/start")
    public ResponseEntity<ScanStartResponse> startScan(@RequestParam(name = "fromTimestamp", required = false) long fromTimeStamp,
                                                       Authentication auth) {
        String userId = (String) auth.getPrincipal();
        ScanStartResponse response = syncService.startScan(UUID.fromString(userId), fromTimeStamp);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/finalize-end")
    public ResponseEntity<ScanResponse> finalizeScan(
            @RequestParam(name = "scanId", required = true) UUID scanId,
            @RequestParam(name = "toTimestamp", required = false) long toTimestamp,
            @RequestBody EndScanRequest endScanRequest, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        ScanResponse response = syncService.finalizeScan(UUID.fromString(userId), scanId, toTimestamp, endScanRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/end")
    public ResponseEntity<ScanResponse> endScan(
            @RequestParam("scanId") UUID scanId, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        ScanResponse response = syncService.endScan(UUID.fromString(userId), scanId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/latest-timestamp")
    public ResponseEntity<SyncMetadataResponse> updateMetadata(
            @RequestParam("timestamp") long timestamp, Authentication auth) {
        String userId = (String) auth.getPrincipal();
        SyncMetadataResponse response = syncService.updateMetadata(UUID.fromString(userId),timestamp);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // Parse SMS Messages in Batch
    // -----------------------------------------------------

    @PostMapping("/batch-upload")
    public ResponseEntity<BatchSyncResponse> uploadBatch(
            @RequestBody BatchSyncRequest request,  Authentication auth) {
        String userId = (String) auth.getPrincipal();
        BatchSyncResponse response = batchService.processBatch(UUID.fromString(userId), request);
        return ResponseEntity.ok(response);
    }
}
