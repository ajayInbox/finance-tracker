package com.finance.tracker.transactions.domain;

import java.util.List;

public record BatchSyncRequest(
    long fromTimestamp,
    List<SmsRequest> smsList
) {}