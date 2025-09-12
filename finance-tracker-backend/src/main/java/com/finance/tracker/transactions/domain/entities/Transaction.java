package com.finance.tracker.transactions.domain.entities;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.transactions.domain.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;

    private String transactionName;
    private Double amount;
    @Enumerated(EnumType.STRING)
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String currency;

    // attachments can be images
    //TODO need to create seperate entity for attachment
    private String attachments;

    // source of data like added from form or added through sms notification etc.
    //TODO need to add seperate entity for external ref
    private String externalRef;

    //TODO need to add seperate entity for user
    private String userId;

}
