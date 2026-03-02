package com.finance.tracker.accounts.domain;

import com.finance.tracker.transactions.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data@AllArgsConstructor
public class BalanceUpdateRequest {

    private UUID accountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private UUID transactionId;

}
