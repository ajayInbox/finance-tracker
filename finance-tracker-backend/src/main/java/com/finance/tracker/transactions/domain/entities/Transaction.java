package com.finance.tracker.transactions.domain.entities;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionSource;
import com.finance.tracker.transactions.domain.TransactionStatus;
import com.finance.tracker.transactions.domain.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_name", nullable = false)
    private String transactionName;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    // --- Business Timestamps (User's Perspective) ---

    @Column(nullable = false)
    private OffsetDateTime occurredAt; // When the user actually spent the money

    private OffsetDateTime postedAt;   // When the bank actually processed it

    // --- System Timestamps (Audit) ---

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt; // DB record creation time (UTC)

    @UpdateTimestamp
    private Instant updatedAt; // DB record last update time (UTC)

    // --- Relationships & Metadata ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tags")
    private List<String> tags;

    private String merchant;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    private String attachments;
    private String externalRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_of")
    private Transaction reversalOf;

    private String lastAction;

    @Enumerated(EnumType.STRING)
    private TransactionSource source;
    private String uniqueIdentifier;
}