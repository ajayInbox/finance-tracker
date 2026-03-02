package com.finance.tracker.transactions.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record BatchUpdateTransactionRequestDto(

        @NotNull(message = "Transaction Id is required")
        UUID id,
        @NotBlank(message = "Transaction name is required")
        @Size(max = 255)
        String transactionName,

        @NotNull(message = "Amount is required")
        @Positive(message = "Amount must be positive")
        BigDecimal amount,

        @NotBlank(message = "Transaction type is required")
        String type, // "INCOME" or "EXPENSE"

        @NotNull(message = "Category ID is required")
        UUID categoryId,

        @NotNull(message = "Account ID is required")
        UUID accountId,

        @NotNull(message = "Occurrence date is required")
        OffsetDateTime occurredAt,

        OffsetDateTime postedAt,

        String merchant,
        String notes,
        List<String> tags,

        @NotBlank
        @Size(min = 3, max = 3)
        String currency // e.g., "INR", "USD"
) {
}
