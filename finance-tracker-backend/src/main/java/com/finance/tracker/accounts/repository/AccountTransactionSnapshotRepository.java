package com.finance.tracker.accounts.repository;

import com.finance.tracker.accounts.domain.entities.AccountTransactionSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountTransactionSnapshotRepository extends JpaRepository<AccountTransactionSnapshot, String> {
}
