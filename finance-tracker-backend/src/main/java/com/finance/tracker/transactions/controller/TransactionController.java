package com.finance.tracker.transactions.controller;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.CreateTransactionRequestDto;
import com.finance.tracker.transactions.domain.dtos.TransactionDto;
import com.finance.tracker.transactions.domain.dtos.TransactionsAverageDto;
import com.finance.tracker.transactions.domain.dtos.UpdateTransactionRequestDto;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.mapper.TransactionMapper;
import com.finance.tracker.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;

    // -----------------------------------------------------
    // Create
    // -----------------------------------------------------
    @PostMapping
    public ResponseEntity<TransactionDto> create(@RequestBody CreateTransactionRequestDto dto) {
        CreateTransactionRequest request = transactionMapper.toRequest(dto);
        Transaction created = transactionService.createNewTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionMapper.toDto(created));
    }

    // -----------------------------------------------------
    // Get Single
    // -----------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getOne(@PathVariable String id) {
        return transactionService.getTransaction(id)
                .map(trx -> ResponseEntity.ok(transactionMapper.toDto(trx)))
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------
    // Get Paginated List (Supports v1 & v2)
    // -----------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "50") int size,
            @RequestParam(name = "version", required = false, defaultValue = "1") int version
    ) {
        PageRequest pageRequest =
                PageRequest.of(page - 1, size, Sort.by("occurred_at").descending());

        return switch (version) {
            case 1 -> {
                Page<Transaction> result = transactionService.getTransactions(pageRequest);
                yield ResponseEntity.ok(result.map(transactionMapper::toResponse));
            }
            case 2 -> {
                Page<TransactionsWithCategoryAndAccount> result =
                        transactionService.getTransactionsV2(pageRequest);
                yield ResponseEntity.ok(result.map(transactionMapper::toDto));
            }
            default -> ResponseEntity.badRequest().body("Invalid API version");
        };
    }

    // -----------------------------------------------------
    // Update
    // -----------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> update(
            @PathVariable("id") String id,
            @RequestBody UpdateTransactionRequestDto dto
    ) {
        UpdateTransactionRequest request = transactionMapper.toRequest(dto);
        Transaction updated = transactionService.updateTransaction(id, request);
        return ResponseEntity.ok(transactionMapper.toDto(updated));
    }

    // -----------------------------------------------------
    // Delete
    // -----------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") String id) {
        return transactionService.getTransaction(id)
                .map(trx -> {
                    transactionService.deleteTransaction(trx);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------
    // Daily Average
    // -----------------------------------------------------
    @PostMapping("/avg-daily")
    public ResponseEntity<TransactionsAverageDto> getDailyAverage(
            @RequestBody SearchRequest searchRequest
    ) {
        TransactionsAverage avg = transactionService.search(searchRequest);
        return ResponseEntity.ok(transactionMapper.toDto(avg));
    }

    // -----------------------------------------------------
    // Monthly Expense Analysis
    // -----------------------------------------------------
    @GetMapping("/analysis")
    public ResponseEntity<MonthlyExpenseResponse> getExpenseAnalysis(
            @RequestParam(name = "duration", required = false) ExpenseReportDuration duration
    ) {
        MonthlyExpenseResponse response = transactionService.getExpenseReport(duration);
        return ResponseEntity.ok(response);
    }

    // -----------------------------------------------------
    // Export SMS Messages
    // -----------------------------------------------------
    @PostMapping("/export-messages")
    public ResponseEntity<Void> exportMessages(@RequestBody List<SmsMessage> messages) {
        transactionService.exportMessagesSendToQueue(messages);
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------
    // Parse SMS Messages
    // -----------------------------------------------------
    @PostMapping("/parse")
    public ResponseEntity<ParsedTransaction> parse(@RequestBody SmsMessage message) {
        ParsedTransaction parsedTransaction = transactionService.parse(message);
        return ResponseEntity.ok(parsedTransaction);
    }
}
