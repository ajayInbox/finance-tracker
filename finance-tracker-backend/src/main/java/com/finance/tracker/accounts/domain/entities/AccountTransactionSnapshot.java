package com.finance.tracker.accounts.domain.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "account_snapshots", indexes = {
        @Index(name = "idx_snapshot_account", columnList = "account_id"),
        @Index(name = "idx_snapshot_transaction", columnList = "transaction_id")
})
public class AccountTransactionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private UUID transactionId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceBefore;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAfter;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal transactionAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}