package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.Currency;
import com.finance.tracker.transactions.domain.TransactionStatus;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.dtos.BatchUpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TransactionBatchRepository {

    private final JdbcTemplate jdbcTemplate;

    public void batchUpdateAndConfirm(List<Transaction> chunk) {

        String sql = """
            UPDATE transactions
            SET
                amount = ?,
                type = ?,
                account_id = ?,
                category_id = ?,
                transaction_name = ?,
                currency = ?,
                occurred_at = ?,
                notes = ?,
                merchant = ?,
                status = ?,
                last_action = ?,
                updated_at = ?
            WHERE id = ? AND status = ?
        """;

        jdbcTemplate.batchUpdate(
                sql,
                chunk,
                chunk.size(),
                (ps, req) -> {

                    ps.setBigDecimal(1, req.getAmount());
                    ps.setObject(2, req.getType().name());
                    ps.setObject(3, req.getAccount().getId());
                    ps.setObject(4, req.getCategory().getId());
                    ps.setString(5, req.getTransactionName());
                    ps.setObject(6, req.getCurrency().name());

                    ps.setObject(7, req.getOccurredAt());

                    ps.setString(8, req.getNotes());
                    ps.setString(9, req.getMerchant());
                    ps.setObject(10, req.getStatus().name());
                    ps.setObject(11, "UPDATED");
                    ps.setTimestamp(12, Timestamp.from(Instant.now()));

                    ps.setObject(13, req.getId());
                    ps.setObject(14, TransactionStatus.DRAFT.name());
                }
        );
    }
}
