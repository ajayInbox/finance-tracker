package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.CategoryExpenseSummary;
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

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String>, JpaSpecificationExecutor<Transaction> {

    @Query(
            value = "SELECT t._id AS _id, t.transaction_name AS transactionName, t.amount AS amount, t.type AS type," +
                    "a._id AS accountId, a.label AS accountName, ats.balance_after AS balanceCached, " +
                    "c._id AS categoryId, c.label AS categoryName, t.occured_at AS occuredAt," +
                    "t.posted_at AS postedAt, t.currency AS currency FROM transaction t " +
                    "INNER JOIN category c ON t.category=c._id INNER JOIN account a ON t.account=a._id INNER JOIN " +
                    "account_transaction_snapshot ats ON t._id=ats.transaction_id and a._id=ats.account_id " +
                    "WHERE t.status='ACTIVE'",
            nativeQuery = true
    )
    Page<TransactionsWithCategoryAndAccount> fetchTransactions(Pageable pageable);

    @Query(value = """
            SELECT c._id, c.label, SUM(t.amount), COUNT(t._id)
           FROM Transaction t
           INNER JOIN category c ON t.category=c._id
           WHERE t.user_id is NULL
             AND t.type = 'EXPENSE'
             AND t.occured_at >= :startTime
            AND t.occured_at <= :endTime
           GROUP BY c._id, c.label
           ORDER BY SUM(t.amount) DESC
        """, nativeQuery = true)
    List<CategoryExpenseSummary> findCategorySummary(
            @Param("userId") Long userId, @Param("startTime") Instant startTime, @Param("endTime") Instant endTime
    );
}
