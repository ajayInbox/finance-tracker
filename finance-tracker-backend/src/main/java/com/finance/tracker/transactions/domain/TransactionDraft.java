package com.finance.tracker.transactions.domain;

import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.SqlResultSetMapping;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@SqlResultSetMapping(
        name = "TransactionDraftMapping",
        classes = @ConstructorResult(
                targetClass = TransactionDraft.class,
                columns = {
                        @ColumnResult(name = "id"),
                        @ColumnResult(name = "transactionName"),
                        @ColumnResult(name = "amount", type = BigDecimal.class),
                        @ColumnResult(name = "type"),
                        @ColumnResult(name = "accountId"),
                        @ColumnResult(name = "accountName"),
                        @ColumnResult(name = "categoryId"),
                        @ColumnResult(name = "categoryName"),
                        @ColumnResult(name = "occurredAt", type = Instant.class),
                        @ColumnResult(name = "postedAt", type = Instant.class),
                        @ColumnResult(name = "currency"),
                        @ColumnResult(name = "originalMessage")
                }
        )
)
@AllArgsConstructor
public class TransactionDraft {

    private String id;

    private String transactionName;
    private BigDecimal amount;
    private String type;
    private String accountId;
    private String accountName;

    private String categoryId;
    private String categoryName;
    private Instant occurredAt;
    private Instant postedAt;
    private String currency;

    private String originalMessage;

}
