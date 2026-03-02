package com.finance.tracker.accounts.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class SnapshotCreateRequest {

    private UUID accountId;
    private UUID transactionId;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private BigDecimal transactionAmount;

}
