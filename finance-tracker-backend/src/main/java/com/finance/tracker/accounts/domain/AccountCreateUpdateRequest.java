package com.finance.tracker.accounts.domain;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AccountCreateUpdateRequest(

        @NotBlank
        String accountName,

        @NotNull
        AccountType accountType,

        @Pattern(regexp = "^\\d{4}$", message = "lastFour must be exactly 4 digits")
        String lastFour,

        @NotBlank
        String currency,

        @NotNull
        LocalDate openingDate,

        BigDecimal startingBalance,       // asset only

        BigDecimal currentOutstanding,    // liability only

        @NotNull
        String statementDayOfMonth,         // credit card only

        @NotNull
        String dueDayOfMonth,            // credit card only

        BigDecimal creditLimit,           // credit card only

        String notes,

        @NotNull
        AccountCategory category           // ASSET / LIABILITY
) {}
