package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.entities.Reconciliation;
import com.finance.tracker.transactions.repository.ReconciliationRepository;
import com.finance.tracker.transactions.service.ReconciliationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReconciliationServiceImpl implements ReconciliationService {

    private final ReconciliationRepository repository;

    @Override
    public void addEntry(Map<String, String> object) {

        Reconciliation newEntry = Reconciliation.builder()
                .originalTxnId(object.get("originalTxnId"))
                .reversalTxnId(object.get("reversalTxnId"))
                .startedAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        repository.save(newEntry);
    }
}
