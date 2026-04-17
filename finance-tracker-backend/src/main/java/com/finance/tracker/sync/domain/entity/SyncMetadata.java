package com.finance.tracker.sync.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_metadata")
@Getter
@Setter
@NoArgsConstructor
public class SyncMetadata {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "last_scanned_sms_date", nullable = false)
    private long lastScannedSmsDate = 0L; // Default to Epoch 0

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PreUpdate
    @PrePersist
    public void updateTimestamp() {
        this.updatedAt = OffsetDateTime.now();
    }
}
