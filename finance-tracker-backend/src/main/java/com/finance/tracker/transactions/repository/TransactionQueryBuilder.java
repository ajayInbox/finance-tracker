package com.finance.tracker.transactions.repository;

import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.entities.Transaction;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import org.aspectj.apache.bcel.generic.Tag;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class TransactionQueryBuilder {

        private TransactionQueryBuilder() {}

        public static Specification<Transaction> forUser(Long userId) {
            return (root, query, cb) -> cb.equal(root.get("user").get("id"), userId);
        }

        public static Specification<Transaction> occurredBetween(LocalDate from, LocalDate to, ZoneId tz) {
            if (from == null && to == null) return Specification.where(null);
            return (root, query, cb) -> {
                Path<Instant> occurredAt = root.get("occurredAt");
                Instant start = from != null ? from.atStartOfDay(tz).toInstant() : Instant.EPOCH;
                Instant end = to != null ? to.plusDays(1).atStartOfDay(tz).toInstant() : Instant.ofEpochMilli(Long.MAX_VALUE);
                return cb.between(occurredAt, start, end);
            };
        }

        public static Specification<Transaction> accountId(Long accountId) {
            if (accountId == null) return Specification.where(null);
            return (root, query, cb) -> cb.equal(root.get("account").get("id"), accountId);
        }

        public static Specification<Transaction> categoryId(Long categoryId) {
            if (categoryId == null) return Specification.where(null);
            return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
        }

        public static Specification<Transaction> type(TransactionType type) {
            if (type == null) return Specification.where(null);
            return (root, query, cb) -> cb.equal(root.get("type"), type);
        }

        public static Specification<Transaction> amountBetween(BigDecimal min, BigDecimal max) {
            if (min == null && max == null) return Specification.where(null);
            return (root, query, cb) -> {
                Path<BigDecimal> amount = root.get("amount");
                List<Predicate> p = new ArrayList<>();
                if (min != null) p.add(cb.greaterThanOrEqualTo(amount, min));
                if (max != null) p.add(cb.lessThanOrEqualTo(amount, max));
                return cb.and(p.toArray(new Predicate[0]));
            };
        }

        public static Specification<Transaction> textSearch(String q) {
            if (q == null || q.isBlank()) return Specification.where(null);
            return (root, query, cb) -> {
                String like = "%" + q.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.get("merchant")), like),
                        cb.like(cb.lower(root.get("notes")), like)
                );
            };
        }

        // Example join on tags if using a join table:
        public static Specification<Transaction> hasAnyTag(Set<String> tagNames) {
            if (tagNames == null || tagNames.isEmpty()) return Specification.where(null);
            return (root, query, cb) -> {
                Join<Transaction, Tag> tag = root.join("tags", JoinType.LEFT);
                query.distinct(true);
                return tag.get("name").in(tagNames);
            };
        }

    public static Specification<Transaction> filters(LocalDate from, LocalDate to, Long accountId, Long categoryId, String q) {
        return (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from.atStartOfDay()));
            if (to != null) p.add(cb.lessThan(root.get("occurredAt"), to.plusDays(1).atStartOfDay()));
            if (accountId != null) p.add(cb.equal(root.get("account").get("id"), accountId));
            if (categoryId != null) p.add(cb.equal(root.get("category").get("id"), categoryId));
            if (q != null && !q.isBlank()) {
                String like = "%" + q.toLowerCase() + "%";
                p.add(cb.or(
                        cb.like(cb.lower(root.get("merchant")), like),
                        cb.like(cb.lower(root.get("notes")), like)
                ));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
    }
}

