package com.finance.tracker.accounts.domain.entities;

import com.finance.tracker.accounts.domain.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "_id")
    private String id;
    private String label;
    @Enumerated(EnumType.STRING)
    private AccountType type;
    private String currency;
    private boolean active;
    private LocalDateTime closedAt;
    private boolean readOnly;

    // if credit card
    private String cardNetwork;
    private String lastFour;
    private String statementDay;
    private String paymentDueDay;
    private Double creditLimit;

    private BigDecimal openingBalance;
    private BigDecimal balanceCached;

    private LocalDateTime createdAt;
    private LocalDateTime balanceAsOf;
    private boolean isAsset;
    private boolean isLiability;

    private String userId;



}
