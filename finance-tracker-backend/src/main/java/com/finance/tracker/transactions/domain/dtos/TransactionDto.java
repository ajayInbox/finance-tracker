package com.finance.tracker.transactions.domain.dtos;

import com.finance.tracker.transactions.domain.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransactionDto {

    private String id;

    private String transactionName;
    private Double amount;
    private TransactionType type;
    private String merchant;
    private String notes;

    // account for example: bank, credit, loan acc etc.
    //TODO need to create seperate entity for account
    private String account;

    // categories for example: Shoping, food, Housing, Income
    //TODO need to create seperate entity for category
    private String category;
    private List<String> tags;
    private LocalDateTime occuredAt;
    private LocalDateTime postedAt;
    private String currency;

    // attachments can be images
    //TODO need to create seperate entity for attachment
    private String attachments;

    // source of data like added from form or added through sms notification etc.
    //TODO need to add seperate entity for external ref
    private String externalRef;

}

