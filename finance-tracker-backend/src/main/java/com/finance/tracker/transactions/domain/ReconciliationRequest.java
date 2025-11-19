package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReconciliationRequest {

    private String originalTxnId;
    private String reversalTxnId;
    private String updatedTxnId;

}
