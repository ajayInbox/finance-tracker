package com.finance.tracker.transactions.domain.dtos;

import com.finance.tracker.transactions.domain.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@Builder
public class TransactionWithCategoryAndAccountDto {

    private String id;

    private String transactionName;
    private Double amount;
    private String type;
    private String accountId;
    private String accountName;
    private BigDecimal balanceCached;

    private String categoryId;
    private String categoryName;
    private Timestamp occuredAt;
    private Timestamp postedAt;
    private String currency;

}
