package com.finance.tracker.sync.service.impl;

import com.finance.tracker.sync.domain.EndScanRequest;
import com.finance.tracker.sync.domain.ScanStatus;
import com.finance.tracker.sync.domain.dtos.ScanResponse;
import com.finance.tracker.sync.domain.dtos.ScanStartResponse;
import com.finance.tracker.sync.domain.dtos.SyncMetadataResponse;
import com.finance.tracker.sync.domain.entity.ScanHistory;
import com.finance.tracker.sync.domain.entity.SyncMetadata;
import com.finance.tracker.sync.exceptions.InvalidScanStateException;
import com.finance.tracker.sync.exceptions.InvalidSyncRequestException;
import com.finance.tracker.sync.exceptions.ScanAccessDeniedException;
import com.finance.tracker.sync.exceptions.ScanNotFoundException;
import com.finance.tracker.sync.repository.ScanHistoryRepository;
import com.finance.tracker.sync.repository.SyncMetadataRepository;
import com.finance.tracker.sync.service.SyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncServiceImpl implements SyncService {

    private final SyncMetadataRepository syncMetadataRepository;
    private final ScanHistoryRepository scanHistoryRepository;

    @Override
    public SyncMetadataResponse getMetadata(UUID userId) {
        SyncMetadata metadata = syncMetadataRepository.findById(userId)
                .orElseGet(() -> {
                    // Initialize metadata if this is the first sync ever
                    SyncMetadata newMeta = new SyncMetadata();
                    newMeta.setUserId(userId);
                    newMeta.setLastScannedSmsDate(0L);
                    return syncMetadataRepository.save(newMeta);
                });

        return new SyncMetadataResponse(
                metadata.getUserId(),
                metadata.getLastScannedSmsDate(),
                "READY"
        );
    }

    @Override
    public ScanStartResponse startScan(UUID userId) {
        ScanHistory existingScan = scanHistoryRepository
                .findFirstByUserIdAndStatusOrderByStartTimeDesc(userId, ScanStatus.STARTED)
                .orElse(null);
        if (existingScan != null) {
            return new ScanStartResponse(
                    existingScan.getId(),
                    existingScan.getStatus().name(),
                    existingScan.getStartTime()
            );
        }

        ScanHistory history = new ScanHistory();
        history.setUserId(userId);
        history.setStatus(ScanStatus.STARTED);
        history.setStartTime(OffsetDateTime.now());

        ScanHistory saved = scanHistoryRepository.save(history);

        return new ScanStartResponse(
                saved.getId(),
                saved.getStatus().name(),
                saved.getStartTime()
        );
    }

    @Override
    public ScanResponse finalizeScan(UUID userId, UUID scanId, EndScanRequest endScanRequest) {
        if (scanId == null) {
            throw new InvalidSyncRequestException("scanId is required");
        }
        if (endScanRequest == null) {
            throw new InvalidSyncRequestException("Scan summary is required");
        }

        ScanHistory history = scanHistoryRepository.findById(scanId)
                .orElseThrow(() -> new ScanNotFoundException("Scan session not found"));

        if (!history.getUserId().equals(userId)) {
            throw new ScanAccessDeniedException("Scan does not belong to the requesting user");
        }
        if (history.getStatus() != ScanStatus.STARTED) {
            throw new InvalidScanStateException("Scan is not in a completable state");
        }

        history.setStatus(ScanStatus.COMPLETED);
        history.setEndTime(OffsetDateTime.now());
        history.setTotalSmsProcessed(endScanRequest.getTotalSmsProcessed());
        history.setTransactionsCreated(endScanRequest.getTransactionsCreated());
        history.setDuplicatesSkipped(endScanRequest.getDuplicatesSkipped());
        history.setFailedToParse(endScanRequest.getFailedToParse());

        ScanHistory scanHistory = scanHistoryRepository.save(history);

        return new ScanResponse(
                scanHistory.getId(),
                scanHistory.getStatus(),
                scanHistory.getStartTime(),
                scanHistory.getEndTime(),
                scanHistory.getTotalSmsProcessed(),
                scanHistory.getTransactionsCreated(),
                scanHistory.getDuplicatesSkipped(),
                scanHistory.getFailedToParse()
        );
    }

    @Override
    public SyncMetadataResponse updateMetadata(UUID userId, long  latestTimestamp) {
        if (latestTimestamp < 0) {
            throw new InvalidSyncRequestException("Timestamp must be non-negative");
        }

        SyncMetadata metadata = syncMetadataRepository.findById(userId)
                .orElseGet(() -> {
                    SyncMetadata newMeta = new SyncMetadata();
                    newMeta.setUserId(userId);
                    newMeta.setLastScannedSmsDate(0L);
                    return newMeta;
                });

        // Only move the bookmark forward, never backward
        if (latestTimestamp < metadata.getLastScannedSmsDate()) {
            throw new InvalidSyncRequestException("Timestamp cannot move backward");
        }
        metadata.setLastScannedSmsDate(latestTimestamp);
        SyncMetadata saved = syncMetadataRepository.save(metadata);
        return new SyncMetadataResponse(
                saved.getUserId(),
                saved.getLastScannedSmsDate(),
                "UPDATED"
        );
    }

    @Override
    public ScanResponse endScan(UUID userId, UUID scanId) {
        if (scanId == null) {
            throw new InvalidSyncRequestException("scanId is required");
        }
        ScanHistory history = scanHistoryRepository.findById(scanId)
                .orElseThrow(() -> new ScanNotFoundException("Scan session not found"));

        if (!history.getUserId().equals(userId)) {
            throw new ScanAccessDeniedException("Scan does not belong to the requesting user");
        }
        if (history.getStatus() != ScanStatus.FAILED) {
            history.setStatus(ScanStatus.COMPLETED);
            history.setEndTime(OffsetDateTime.now());
            ScanHistory scanHistory = scanHistoryRepository.save(history);
            return new ScanResponse(
                    scanHistory.getId(),
                    scanHistory.getStatus(),
                    scanHistory.getStartTime(),
                    scanHistory.getEndTime(),
                    scanHistory.getTotalSmsProcessed(),
                    scanHistory.getTransactionsCreated(),
                    scanHistory.getDuplicatesSkipped(),
                    scanHistory.getFailedToParse()
            );
        }
        return new ScanResponse(
                history.getId(),
                history.getStatus(),
                history.getStartTime(),
                history.getEndTime(),
                history.getTotalSmsProcessed(),
                history.getTransactionsCreated(),
                history.getDuplicatesSkipped(),
                history.getFailedToParse()
        );
    }
}
