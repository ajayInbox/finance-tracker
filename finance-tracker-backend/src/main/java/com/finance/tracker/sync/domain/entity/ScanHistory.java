package com.finance.tracker.sync.domain.entity;

import com.finance.tracker.sync.domain.ScanStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "scan_history")
@Getter
@Setter
@NoArgsConstructor
public class ScanHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id; // This is the scanId you'll return to Flutter

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScanStatus status;

    @Column(name = "start_time", nullable = false)
    private OffsetDateTime startTime;

    @Column(name = "end_time")
    private OffsetDateTime endTime;

    // Detailed stats for the developer dashboard
    private int totalSmsProcessed;
    private int transactionsCreated;
    private int duplicatesSkipped;
    private int failedToParse;
}