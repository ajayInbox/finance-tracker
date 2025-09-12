package com.finance.tracker.accounts.repository;

import com.finance.tracker.accounts.domain.entities.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    @Query(value = "SELECT * FROM ACCOUNT WHERE _id=:accountId AND (user_id IS NULL OR user_id = :userId) and active=true LIMIT 1", nativeQuery = true)
    Optional<Account> findAccountByIdForUser(@Param("accountId") String accountId, @Param("userId") String userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :accountId AND (a.userId = :userId OR a.userId IS NULL)")
    Optional<Account> lockAccount(
            @Param("accountId") String accountId,
            @Param("userId") String userId);

}
