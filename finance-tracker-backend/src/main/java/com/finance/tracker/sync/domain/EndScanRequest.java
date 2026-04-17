package com.finance.tracker.sync.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class EndScanRequest {

    private int totalSmsProcessed;
    private int transactionsCreated;
    private int duplicatesSkipped;
    private int failedToParse;

}
