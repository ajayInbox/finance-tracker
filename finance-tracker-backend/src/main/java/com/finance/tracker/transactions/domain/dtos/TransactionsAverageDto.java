package com.finance.tracker.transactions.domain.dtos;

import com.finance.tracker.transactions.domain.TransactionDaily;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransactionsAverageDto {

    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private int days;

    private List<TransactionDaily> dailyList;
    private Double averageDailyExpense;

}
