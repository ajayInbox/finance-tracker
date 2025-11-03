package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.entities.Reconciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReconciliationRepository extends JpaRepository<Reconciliation, String> {
}
