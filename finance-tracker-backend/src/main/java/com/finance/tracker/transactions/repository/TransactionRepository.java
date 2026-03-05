package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.CategoryExpenseSummary;
import com.finance.tracker.transactions.domain.TransactionDraftProjection;
import com.finance.tracker.transactions.domain.TransactionStatus;
import com.finance.tracker.transactions.domain.TransactionsWithCategoryAndAccount;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @Query(
            value = "SELECT t._id AS _id, t.transaction_name AS transactionName, t.amount AS amount, t.type AS type," +
                    "a._id AS accountId, a.account_name AS accountName, ats.balance_after AS balanceCached, " +
                    "c.id AS categoryId, c.name AS categoryName, t.occurred_at AS occuredAt," +
                    "t.posted_at AS postedAt, t.currency AS currency FROM transaction t " +
                    "INNER JOIN categories c ON t.category=c.id INNER JOIN account a ON t.account=a._id INNER JOIN " +
                    "account_transaction_snapshot ats ON t._id=ats.transaction_id and a._id=ats.account_id " +
                    "WHERE t.status=:status",
            nativeQuery = true
    )
    Page<TransactionsWithCategoryAndAccount> fetchTransactions(@Param("status") String status, Pageable pageable);

    @Query(value = """
            SELECT c.id, c.name, SUM(t.amount), COUNT(t._id)
           FROM Transaction t
           INNER JOIN categories c ON t.category=c.id
           WHERE t.user_id is NULL
             AND t.type = 'EXPENSE'
             AND t.occurred_at >= :startTime
            AND t.occurred_at <= :endTime
            AND t.status = 'ACTIVE'
           GROUP BY c.id, c.name
           ORDER BY SUM(t.amount) DESC
        """, nativeQuery = true)
    List<CategoryExpenseSummary> findCategorySummary(
            @Param("userId") Long userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime
    );

    @Query(value = "SELECT * from transaction where unique_identifier=:uniqueIdentifier AND (status='DRAFT' OR status='ACTIVE')", nativeQuery = true)
    Optional<Transaction> findTransactionByUniqueIdentifier(@Param("uniqueIdentifier") String uniqueIdentifier);

    @Query(value = """
    SELECT t._id AS id,
        t.transaction_name AS transactionName,
        t.amount AS amount,
        t.type AS type,
        t.account AS accountId,
        a.account_name AS accountName,
        t.category AS categoryId,
        c.name AS categoryName,
        t.occurred_at AS occurredAt,
        t.posted_at AS postedAt,
        t.currency AS currency,
        t.original_message AS originalMessage
    FROM transaction t
    LEFT JOIN account a ON t.account = a._id LEFT JOIN categories c ON t.category = c.id WHERE t.status = 'DRAFT'
   """,
            nativeQuery = true)
    List<TransactionDraftProjection> findDraftTransactions();

    Optional<Transaction> findByIdAndUserId(UUID transactionId, UUID userId);

    Page<Transaction> findAllByUserIdAndStatus(UUID userId, TransactionStatus status, Pageable pageable);

}
