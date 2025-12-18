package com.finance.tracker.transactions.domain.entities;

import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.LastAction;
import com.finance.tracker.transactions.domain.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transaction", indexes = {
        @Index(name = "idx_account", columnList = "account"),
        @Index(name = "idx_category", columnList = "category"),
        @Index(name = "idx_occurredAt", columnList = "occurredAt"),
        @Index(name = "idx_reversalOf", columnList = "reversalOf")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;

    private String transactionName;
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String merchant;
    private String notes;

    private String account;
    private String category;

    @Transient
    private List<String> tags;

    private Instant occurredAt;
    private Instant postedAt;

    private Instant createdAt;
    private Instant updatedAt;

    private String currency;
    private String attachments;
    private String externalRef;
    private String userId;

    @Enumerated(EnumType.STRING)
    private LastAction lastAction;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    // Points to ORIGINAL transaction ID
    private String reversalOf;

    public Transaction(Transaction original) {
        this.transactionName = original.transactionName;
        this.amount = original.amount;
        this.type = original.type;
        this.currency = original.currency;
        this.account = original.account;
        this.category = original.category;
        this.merchant = original.merchant;
        this.notes = original.notes;
        this.occurredAt = original.occurredAt;
        this.postedAt = original.postedAt;
        this.tags = original.tags == null ? null : List.copyOf(original.tags);
        this.attachments = original.attachments;
        this.externalRef = original.externalRef;
        this.userId = original.userId;
    }
}
