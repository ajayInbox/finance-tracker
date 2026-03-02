package com.finance.tracker.accounts.repository;

import com.finance.tracker.accounts.domain.AccountType;
import com.finance.tracker.accounts.domain.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    @Query("SELECT a FROM Account a WHERE a.id = :id AND a.userId = :userId")
    Optional<Account> findAccountByIdForUser(UUID id, UUID userId);

    List<Account> findByUserIdAndActiveTrue(UUID userId);

    Optional<Account> findByLastFourAndUserIdAndAccountType(String lastFour, UUID userId, AccountType accountType);

    @Modifying
    @Query("""
        UPDATE Account a 
        SET a.currentBalance = a.currentBalance + :delta, a.balanceAsOf = CURRENT_TIMESTAMP
        WHERE a.id = :id AND a.userId = :userId AND a.active = true 
        AND (a.currentBalance + :delta) >= 0
    """)
    int updateAssetBalance(UUID id, UUID userId, BigDecimal delta);

    @Modifying
    @Query("""
        UPDATE Account a 
        SET a.currentOutstanding = a.currentOutstanding + :delta, a.balanceAsOf = CURRENT_TIMESTAMP
        WHERE a.id = :id AND a.userId = :userId AND a.active = true 
        AND (a.currentOutstanding + :delta) >= 0
    """)
    int updateLiabilityBalance(UUID id, UUID userId, BigDecimal delta);
}