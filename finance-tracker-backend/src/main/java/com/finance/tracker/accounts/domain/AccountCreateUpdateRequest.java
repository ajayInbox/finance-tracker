package com.finance.tracker.accounts.domain;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountCreateUpdateRequest(
        @NotBlank
        String accountName,
        @NotNull
        AccountType accountType,
        @Pattern(regexp = "^\\d{4}$", message = "lastFour must be exactly 4 digits")
        String lastFour,
        String currency,
        BigDecimal startingBalance,       // asset only
        BigDecimal currentOutstanding,    // liability only
        int statementDayOfMonth,         // credit card only
        int dueDayOfMonth,            // credit card only
        BigDecimal creditLimit,           // credit card only
        String notes,
        @NotBlank(message = "category should be present. value is either ASSET or LIABILITY")
        AccountCategory category           // ASSET / LIABILITY
) {}
