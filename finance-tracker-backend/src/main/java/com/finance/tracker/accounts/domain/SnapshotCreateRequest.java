package com.finance.tracker.accounts.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class SnapshotCreateRequest {

    private String accountId;
    private String transactionId;
    private BigDecimal previousBalance;
    private BigDecimal newBalance;
    private Double transactionAmount;

}
