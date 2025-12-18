package com.finance.tracker.accounts.service.impl;

import com.finance.tracker.accounts.domain.SnapshotCreateRequest;
import com.finance.tracker.accounts.domain.entities.AccountTransactionSnapshot;
import com.finance.tracker.accounts.repository.AccountTransactionSnapshotRepository;
import com.finance.tracker.accounts.service.AccountTransactionSnapshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountTransactionSnapshotServiceImpl implements AccountTransactionSnapshotService {

    private final AccountTransactionSnapshotRepository snapshotRepository;

    @Override
    public void createSnapshot(SnapshotCreateRequest snapshotCreateRequest) {

        AccountTransactionSnapshot newSnapshot = AccountTransactionSnapshot.builder()
                .accountId(snapshotCreateRequest.getAccountId())
                .transactionId(snapshotCreateRequest.getTransactionId())
                .balanceBefore(snapshotCreateRequest.getPreviousBalance())
                .balanceAfter(snapshotCreateRequest.getNewBalance())
                .transactionAmount(snapshotCreateRequest.getTransactionAmount())
                .createdAt(Instant.now())
                .build();
        snapshotRepository.save(newSnapshot);
    }
}
