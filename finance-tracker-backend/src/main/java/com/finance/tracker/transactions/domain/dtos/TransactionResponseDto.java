package com.finance.tracker.transactions.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class TransactionResponseDto {

    private UUID id;
    private String transactionName;
    private BigDecimal amount;
    private String type;
    private UUID categoryId;
    private String categoryName;
    private UUID accountId;
    private String accountName;
    private LocalDateTime occurredAt;
    private List<String> tags;
    private String status;
    private String originalMessage;
    private String merchant;
    private String notes;

}
