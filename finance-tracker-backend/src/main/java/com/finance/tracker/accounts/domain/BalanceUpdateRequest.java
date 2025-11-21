package com.finance.tracker.accounts.domain;

import com.finance.tracker.transactions.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data@AllArgsConstructor
public class BalanceUpdateRequest {

    private String accountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private String transactionId;

}
