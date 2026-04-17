package com.finance.tracker.sync.repository;

import com.finance.tracker.sync.domain.entity.SyncMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SyncMetadataRepository extends JpaRepository<SyncMetadata, UUID> {
}