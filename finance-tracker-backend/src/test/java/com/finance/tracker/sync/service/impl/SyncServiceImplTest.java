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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceImplTest {

    @Mock
    private SyncMetadataRepository syncMetadataRepository;

    @Mock
    private ScanHistoryRepository scanHistoryRepository;

    @InjectMocks
    private SyncServiceImpl syncService;

    private UUID userId;
    private UUID scanId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        scanId = UUID.randomUUID();
    }

    @Test
    void startScanReturnsExistingStartedScanForUser() {
        ScanHistory existing = new ScanHistory();
        existing.setId(scanId);
        existing.setUserId(userId);
        existing.setStatus(ScanStatus.STARTED);
        existing.setStartTime(OffsetDateTime.now().minusMinutes(1));

        when(scanHistoryRepository.findFirstByUserIdAndStatusOrderByStartTimeDesc(userId, ScanStatus.STARTED))
                .thenReturn(Optional.of(existing));

        ScanStartResponse response = syncService.startScan(userId);

        assertEquals(scanId, response.scanId());
        assertEquals(ScanStatus.STARTED.name(), response.status());
        verify(scanHistoryRepository, never()).save(any(ScanHistory.class));
    }

    @Test
    void finalizeScanRejectsScanOwnedByAnotherUser() {
        ScanHistory existing = new ScanHistory();
        existing.setId(scanId);
        existing.setUserId(UUID.randomUUID());
        existing.setStatus(ScanStatus.STARTED);

        when(scanHistoryRepository.findById(scanId)).thenReturn(Optional.of(existing));

        assertThrows(ScanAccessDeniedException.class,
                () -> syncService.finalizeScan(userId, scanId, EndScanRequest.builder().build()));
    }

    @Test
    void finalizeScanRejectsAlreadyCompletedScan() {
        ScanHistory existing = new ScanHistory();
        existing.setId(scanId);
        existing.setUserId(userId);
        existing.setStatus(ScanStatus.COMPLETED);

        when(scanHistoryRepository.findById(scanId)).thenReturn(Optional.of(existing));

        assertThrows(InvalidScanStateException.class,
                () -> syncService.finalizeScan(userId, scanId, EndScanRequest.builder().build()));
    }

    @Test
    void finalizeScanUpdatesCountersAndCompletesStartedScan() {
        ScanHistory existing = new ScanHistory();
        existing.setId(scanId);
        existing.setUserId(userId);
        existing.setStatus(ScanStatus.STARTED);
        existing.setStartTime(OffsetDateTime.now().minusMinutes(2));

        EndScanRequest request = EndScanRequest.builder()
                .totalSmsProcessed(10)
                .transactionsCreated(7)
                .duplicatesSkipped(2)
                .failedToParse(1)
                .build();

        when(scanHistoryRepository.findById(scanId)).thenReturn(Optional.of(existing));
        when(scanHistoryRepository.save(existing)).thenReturn(existing);

        ScanResponse response = syncService.finalizeScan(userId, scanId, request);

        assertEquals(ScanStatus.COMPLETED, response.status());
        assertEquals(10, response.totalSmsProcessed());
        assertEquals(7, response.transactionsCreated());
        assertEquals(2, response.duplicatesSkipped());
        assertEquals(1, response.failedToParse());
        assertNotNull(existing.getEndTime());
    }

    @Test
    void finalizeScanThrowsWhenScanDoesNotExist() {
        when(scanHistoryRepository.findById(scanId)).thenReturn(Optional.empty());

        assertThrows(ScanNotFoundException.class,
                () -> syncService.finalizeScan(userId, scanId, EndScanRequest.builder().build()));
    }

    @Test
    void updateMetadataInitializesMissingRecord() {
        ArgumentCaptor<SyncMetadata> captor = ArgumentCaptor.forClass(SyncMetadata.class);

        when(syncMetadataRepository.findById(userId)).thenReturn(Optional.empty());
        when(syncMetadataRepository.save(any(SyncMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SyncMetadataResponse response = syncService.updateMetadata(userId, 12345L);

        assertEquals(userId, response.userId());
        assertEquals(12345L, response.latestScannedTimestamp());
        verify(syncMetadataRepository).save(captor.capture());
        assertEquals(userId, captor.getValue().getUserId());
        assertEquals(12345L, captor.getValue().getLastScannedSmsDate());
    }

    @Test
    void updateMetadataRejectsBackwardTimestamp() {
        SyncMetadata metadata = new SyncMetadata();
        metadata.setUserId(userId);
        metadata.setLastScannedSmsDate(500L);

        when(syncMetadataRepository.findById(userId)).thenReturn(Optional.of(metadata));

        assertThrows(InvalidSyncRequestException.class, () -> syncService.updateMetadata(userId, 499L));
        verify(syncMetadataRepository, never()).save(any(SyncMetadata.class));
    }
}
