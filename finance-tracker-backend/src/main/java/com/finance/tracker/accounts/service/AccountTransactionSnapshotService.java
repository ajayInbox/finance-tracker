package com.finance.tracker.accounts.service;

import com.finance.tracker.accounts.domain.SnapshotCreateRequest;

public interface AccountTransactionSnapshotService {
    void createSnapshot(SnapshotCreateRequest snapshotCreateRequest);
}
