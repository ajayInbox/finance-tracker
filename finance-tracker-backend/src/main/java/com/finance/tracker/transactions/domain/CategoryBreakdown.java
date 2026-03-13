package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CategoryBreakdown {

    private UUID categoryId;
    private String categoryName;
    private BigDecimal total;
    private BigDecimal percentage;
    private int transactionCount;

}
