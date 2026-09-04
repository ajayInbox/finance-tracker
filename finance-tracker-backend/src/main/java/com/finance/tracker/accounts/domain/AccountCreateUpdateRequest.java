package com.finance.tracker.accounts.domain;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AccountCreateUpdateRequest(
        @JsonAlias({"name", "accountName"})
        String accountName,
        @JsonAlias({"type", "accountType"})
        @NotNull
        AccountType accountType,
        String lastFour,
        String institution,
        String currency,
        @JsonAlias({"balance", "startingBalance"})
        BigDecimal startingBalance,       // asset only
        @JsonAlias({"balance", "currentOutstanding"})
        BigDecimal currentOutstanding,    // liability only
        int statementDayOfMonth,         // credit card only
        int dueDayOfMonth,            // credit card only
        BigDecimal creditLimit,           // credit card only
        String notes,
        @NotNull(message = "category should be present. value is either ASSET or LIABILITY")
        AccountCategory category           // ASSET / LIABILITY
) {
    public AccountCreateUpdateRequest(
            String accountName,
            AccountType accountType,
            String lastFour,
            String currency,
            BigDecimal startingBalance,
            BigDecimal currentOutstanding,
            int statementDayOfMonth,
            int dueDayOfMonth,
            BigDecimal creditLimit,
            String notes,
            AccountCategory category
    ) {
        this(accountName, accountType, lastFour, null, currency, startingBalance, currentOutstanding, statementDayOfMonth, dueDayOfMonth, creditLimit, notes, category);
    }

    public String name() {
        return accountName != null ? accountName : "";
    }

    public AccountType type() {
        return accountType;
    }

    public BigDecimal balance() {
        return startingBalance != null ? startingBalance : (currentOutstanding != null ? currentOutstanding : BigDecimal.ZERO);
    }
}

