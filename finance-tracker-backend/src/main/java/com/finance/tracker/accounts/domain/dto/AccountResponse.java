package com.finance.tracker.accounts.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AccountResponse(
        String id,
        String accountName,
        AccountType accountType,
        String lastFour,
        String institution,
        String currency,
        LocalDate openingDate,
        BigDecimal startingBalance,
        BigDecimal currentOutstanding,
        String statementDayOfMonth,
        String dueDayOfMonth,
        BigDecimal creditLimit,
        BigDecimal currentBalance,
        @JsonProperty("active")
        boolean active,
        boolean readOnly,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        String notes,
        AccountCategory category,
        boolean isDefault
) {
    public AccountResponse(
            String id,
            String accountName,
            AccountType accountType,
            String lastFour,
            String currency,
            LocalDate openingDate,
            BigDecimal startingBalance,
            BigDecimal currentOutstanding,
            String statementDayOfMonth,
            String dueDayOfMonth,
            BigDecimal creditLimit,
            BigDecimal currentBalance,
            boolean active,
            boolean readOnly,
            LocalDateTime createdAt,
            LocalDateTime closedAt,
            String notes,
            AccountCategory category,
            boolean isDefault
    ) {
        this(id, accountName, accountType, lastFour, null, currency, openingDate, startingBalance, currentOutstanding, statementDayOfMonth, dueDayOfMonth, creditLimit, currentBalance, active, readOnly, createdAt, closedAt, notes, category, isDefault);
    }

    @JsonProperty("name")
    public String name() {
        return accountName;
    }

    @JsonProperty("type")
    public AccountType type() {
        return accountType;
    }

    @JsonProperty("accountNumber")
    public String accountNumber() {
        return lastFour;
    }

    @JsonProperty("balance")
    public BigDecimal balance() {
        if (currentBalance != null) {
            return currentBalance;
        }
        if (startingBalance != null) {
            return startingBalance;
        }
        if (currentOutstanding != null) {
            return currentOutstanding;
        }
        return BigDecimal.ZERO;
    }

    @JsonProperty("isActive")
    public boolean getIsActive() {
        return active;
    }

    @JsonProperty("availableCredit")
    public BigDecimal availableCredit() {
        if (creditLimit != null && currentOutstanding != null) {
            return creditLimit.subtract(currentOutstanding);
        }
        return creditLimit;
    }
}

