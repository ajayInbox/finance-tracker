package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTransactionRequest {
    
    BigDecimal amount;
    String type;
    String account;
    String category;
    String merchant;
    String notes;
    String transactionName;
    List<String> tags;
    String occurredAt;
    String postedAt;
    String currency;
    String attachments;
    String externalRef;
            
}

