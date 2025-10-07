package com.finance.tracker.transactions.domain;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class TransactionsAverage {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private int days;

    private List<TransactionDaily> dailyList;
    private Double averageDailyExpense;

}
