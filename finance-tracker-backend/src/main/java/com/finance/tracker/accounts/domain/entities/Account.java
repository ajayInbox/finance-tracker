package com.finance.tracker.accounts.domain.entities;

import com.finance.tracker.accounts.domain.*;
import com.finance.tracker.transactions.domain.Currency;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "accounts", indexes = {
        @Index(name = "idx_account_user", columnList = "user_id"),
        @Index(name = "idx_account_status", columnList = "status")
})
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(length = 4, nullable = false)
    private String lastFour;

    @Enumerated(EnumType.STRING)
    private Currency currency;

    private LocalDate openingDate;

    // Use explicit defaults
    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean readOnly = false;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;

    private Instant closedAt;

    private String notes;

    @Enumerated(EnumType.STRING)
    private AccountCategory category;

    // Concurrency control: Crucial for financial balance updates
    @Version
    private Long version;

    // Precision for financial accuracy (19 digits total, 4 after decimal)
    @Column(precision = 19, scale = 2)
    private BigDecimal startingBalance;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(precision = 19, scale = 2)
    private BigDecimal currentOutstanding;

    @Column(precision = 19, scale = 2)
    private BigDecimal creditLimit;

    private Integer statementDayOfMonth; // Changed from String
    private Integer dueDayOfMonth;       // Changed from String

    private Instant balanceAsOf;

    // Use UUID for foreign keys to ensure DB performance
    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    public boolean isAsset() {
        return category == AccountCategory.ASSET;
    }

    public boolean isLiability() {
        return category == AccountCategory.LIABILITY;
    }
}