package com.finance.tracker.accounts.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountTransactionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;
    private String accountId;
    private String transactionId;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private BigDecimal transactionAmount;
    private Instant createdAt;

}
