package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.ReconciliationRequest;
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
    public void addEntry(ReconciliationRequest request) {

        Reconciliation newEntry = Reconciliation.builder()
                .originalTxnId(request.getOriginalTxnId())
                .reversalTxnId(request.getReversalTxnId())
                .updatedTxnId(request.getUpdatedTxnId())
                .createdAt(LocalDateTime.now())
                .status("PENDING")
                .build();

        repository.save(newEntry);
    }
}
