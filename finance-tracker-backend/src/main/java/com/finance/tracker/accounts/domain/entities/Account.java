package com.finance.tracker.accounts.domain.entities;

import com.finance.tracker.accounts.domain.AccountCategory;
import com.finance.tracker.accounts.domain.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    private String accountName;

    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(length = 4, nullable = false)
    private String lastFour;

    private String currency;

    private LocalDate openingDate;

    private boolean active = true;
    private boolean readOnly = false;

    private LocalDateTime createdAt;
    private LocalDateTime closedAt;

    private String notes;

    @Enumerated(EnumType.STRING)
    private AccountCategory category;

    // ------ ASSET fields ------
    private BigDecimal startingBalance;
    private BigDecimal currentBalance;

    // ------ LIABILITY fields ------
    private BigDecimal currentOutstanding;
    private BigDecimal creditLimit;
    private Integer cutoffDayOfMonth;
    private Integer dueDayOfMonth;

    private LocalDateTime balanceAsOf;

    private String userId;

    // Helpers
    public boolean isAsset() {
        return category == AccountCategory.ASSET;
    }

    public boolean isLiability() {
        return category == AccountCategory.LIABILITY;
    }
}
