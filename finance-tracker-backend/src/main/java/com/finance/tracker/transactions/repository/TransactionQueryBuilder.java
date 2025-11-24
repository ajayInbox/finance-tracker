package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.entities.Transaction;
import jakarta.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;

import java.time.*;

public final class TransactionQueryBuilder {

    private TransactionQueryBuilder() {}

    public static Specification<Transaction> occurredBetween(Instant fromDate, Instant toDate) {
        return (root, query, cb) -> {
            Path<Instant> occurredAt = root.get("occurredAt");
           // return cb.between(occurredAt, start, end);
            return cb.and(
                    cb.greaterThanOrEqualTo(occurredAt, fromDate),
                    cb.lessThanOrEqualTo(occurredAt, toDate)
            );
        };
    }

    public static Specification<Transaction> type(TransactionType type) {
        return (root, query, cb) ->
                type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }
}

