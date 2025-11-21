package com.finance.tracker.accounts.domain.dto;

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
        String currency,
        LocalDate openingDate,
        BigDecimal startingBalance,
        BigDecimal currentOutstanding,
        Integer cutoffDayOfMonth,
        Integer dueDayOfMonth,
        BigDecimal creditLimit,
        BigDecimal currentBalance,
        boolean active,
        boolean readOnly,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        String notes,
        AccountCategory category
) {}
