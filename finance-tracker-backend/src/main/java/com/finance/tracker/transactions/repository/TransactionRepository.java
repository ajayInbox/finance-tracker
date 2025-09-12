package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.TransactionsWithCategoryAndAccount;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query(
            value = "SELECT t._id AS _id, t.transaction_name AS transactionName, t.amount AS amount, t.type AS type," +
                    "a._id AS accountId, a.label AS accountName, a.balance_cached AS balanceCached, " +
                    "c._id AS categoryId, c.label AS categoryName, t.occured_at AS occuredAt," +
                    "t.posted_at AS postedAt, t.currency AS currency FROM transaction t " +
                    "INNER JOIN category c ON t.category=c._id INNER JOIN account a ON t.account=a._id",
            nativeQuery = true
    )
    Page<TransactionsWithCategoryAndAccount> fetchTransactions(Pageable pageable);
}
