package com.finance.tracker.accounts.domain.dto;

import com.finance.tracker.accounts.domain.AccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDto {

    private String id;
    private String label;
    private AccountType type;
    private String currency;
    private Boolean active;
    private LocalDateTime closedAt;
    private Boolean readOnly;

    // if credit card
    private String cardNetwork;
    private String lastFour;
    private String statementDay;
    private String paymentDueDay;
    private Double creditLimit;

    private BigDecimal openingBalance;
    private BigDecimal balanceCached;

    private LocalDateTime createdAt;
    private LocalDateTime balanceAsOf;
}
