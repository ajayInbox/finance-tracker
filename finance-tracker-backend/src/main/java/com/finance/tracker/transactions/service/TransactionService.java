package com.finance.tracker.transactions.service;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.CreateTransactionRequestDto;
import com.finance.tracker.transactions.domain.dtos.TransactionResponseDto;
import com.finance.tracker.transactions.domain.dtos.UpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionService {
    TransactionResponseDto create(CreateTransactionRequestDto request, UUID userId);

    Optional<Transaction> getTransaction(UUID id);

    List<TransactionResponseDto> getTransactions(Pageable pageable);

    Page<TransactionsWithCategoryAndAccount> getTransactionsV2(String status, Pageable pageable);

    TransactionsAverage search(SearchRequest searchRequest);

    MonthlyExpenseResponse getExpenseReport(UUID userId, ExpenseReportRequest duration);

    void exportMessages(List<SmsRequest> messageList);

    void exportMessagesSendToQueue(List<SmsRequest> messageList);

    void createTransactionFromQueueMsg(SmsRequest message);

    void deleteTransaction(UUID userId, Transaction transaction);

    TransactionResponseDto update(UUID userId, UUID trxId, UpdateTransactionRequestDto request);

    ParsedTxnResponse parse(UUID userId, SmsRequest message);

    List<TransactionResponseDto> getAll(UUID userId, TransactionStatus status, Pageable pageable);

    TransactionResponseDto mapToResponseDto(Transaction txn);
}
