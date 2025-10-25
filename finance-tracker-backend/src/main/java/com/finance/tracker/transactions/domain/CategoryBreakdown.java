package com.finance.tracker.transactions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CategoryBreakdown {

    private String categoryId;
    private String categoryName;
    private BigDecimal total;
    private BigDecimal percentage;
    private int transactionCount;

}
