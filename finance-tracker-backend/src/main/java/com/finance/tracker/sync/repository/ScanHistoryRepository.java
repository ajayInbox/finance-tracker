package com.finance.tracker.sync.repository;

import com.finance.tracker.sync.domain.entity.ScanHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ScanHistoryRepository extends JpaRepository<ScanHistory, UUID> {
    java.util.Optional<ScanHistory> findFirstByUserIdAndStatusOrderByStartTimeDesc(UUID userId, com.finance.tracker.sync.domain.ScanStatus status);
}
