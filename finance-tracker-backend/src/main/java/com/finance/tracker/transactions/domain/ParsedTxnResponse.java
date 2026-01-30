package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParsedTxnResponse {

    private String status;
    private String uniqueIdentifier;
    private ParsedTransaction parsedTransaction;

}
