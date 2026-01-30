package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.entities.UnparsedSmsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnparsedSmsLogsRepository extends JpaRepository<UnparsedSmsLog, String> {
}
