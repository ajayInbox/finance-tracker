package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionStatus;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.dtos.BatchUpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdateAndConfirm(List<Transaction> chunk) {

        String sql = """
            UPDATE transaction
            SET
                amount = ?,
                type = ?,
                account = ?,
                category = ?,
                transaction_name = ?,
                currency = ?,
                occurred_at = ?,
                notes = ?,
                merchant = ?,
                status = ?
            WHERE _id = ? AND status = ?
        """;

        jdbcTemplate.batchUpdate(
                sql,
                chunk,
                chunk.size(),
                (ps, req) -> {

                    ps.setBigDecimal(1, req.getAmount());
                    ps.setObject(2, req.getType());
                    ps.setObject(3, req.getAccount());
                    ps.setObject(4, req.getCategory());
                    ps.setString(5, req.getTransactionName());
                    ps.setObject(6, req.getCurrency());

                    ps.setObject(7, req.getOccurredAt());

                    ps.setString(8, req.getNotes());
                    ps.setString(9, req.getMerchant());
                    ps.setObject(10, req.getStatus());

                    ps.setObject(11, req.getId());
                    ps.setObject(12, TransactionStatus.DRAFT);
                }
        );
    }
}
