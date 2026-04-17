package com.finance.tracker.transactions.domain;

import java.util.List;
import java.util.UUID;

public record BatchSyncRequest(
    UUID scanId,
    List<SmsRequest> smsList
) {}