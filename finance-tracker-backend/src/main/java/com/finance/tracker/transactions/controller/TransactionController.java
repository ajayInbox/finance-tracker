package com.finance.tracker.transactions.controller;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.*;
import com.finance.tracker.transactions.service.TransactionBatchService;
import com.finance.tracker.transactions.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionBatchService batchService;

    // -----------------------------------------------------
    // Create
    // -----------------------------------------------------
    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(@Valid @RequestBody CreateTransactionRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(dto, UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4")));
    }

    // -----------------------------------------------------
    // Get Single
    // -----------------------------------------------------
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> getOne(@PathVariable("id") UUID id) {
        return transactionService.getTransaction(id)
                .map(trx -> ResponseEntity.ok(transactionService.mapToResponseDto(trx)))
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------
    // Get Paginated List (Supports v1 & v2)
    // -----------------------------------------------------
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(name = "page", required = false, defaultValue = "1") int page,
            @RequestParam(name = "size", required = false, defaultValue = "50") int size,
            @RequestParam(name = "version", required = false, defaultValue = "1") int version,
            @RequestParam(name = "status", required = false, defaultValue = "CONFIRMED") String status
    ) {
        PageRequest pageRequest =
                PageRequest.of(page - 1, size, Sort.by("occurredAt").descending());
        UUID userId = UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4");

        return switch (version) {
            case 1 -> {
                List<TransactionResponseDto> result = transactionService.getAll(userId, TransactionStatus.CONFIRMED, pageRequest);
                yield ResponseEntity.ok(result);
            }
            case 2 -> {
                yield ResponseEntity.ok(transactionService.getTransactions(pageRequest));
            }
            case 3 -> {
                List<TransactionResponseDto> result = transactionService.getAll(userId, TransactionStatus.DRAFT, pageRequest);
                yield ResponseEntity.ok(result);
            }
            default -> ResponseEntity.badRequest().body("Invalid API version");
        };
    }

    // -----------------------------------------------------
    // Update
    // -----------------------------------------------------
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponseDto> update(
            @PathVariable("id") UUID id,
            @Valid @RequestBody UpdateTransactionRequestDto dto
    ) {
        return ResponseEntity.ok(transactionService.update(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), id, dto));
    }

    // -----------------------------------------------------
    // Delete
    // -----------------------------------------------------
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") UUID id) {
        return transactionService.getTransaction(id)
                .map(trx -> {
                    transactionService.deleteTransaction(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), trx);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // -----------------------------------------------------
    // Daily Average
    // -----------------------------------------------------
    @PostMapping("/avg-daily")
    public ResponseEntity<TransactionsAverage> getDailyAverage(
            @RequestBody SearchRequest searchRequest
    ) {
        TransactionsAverage avg = transactionService.search(searchRequest);
        return ResponseEntity.ok(avg);
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
    public ResponseEntity<Void> exportMessages(@RequestBody List<SmsRequest> messages) {
        transactionService.exportMessagesSendToQueue(messages);
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------
    // Parse SMS Messages
    // -----------------------------------------------------
    @PostMapping("/parse")
    public ResponseEntity<ParsedTxnResponse> parse(@RequestBody SmsRequest message) {
        return ResponseEntity.ok(transactionService.parse(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), message));
    }

    // -----------------------------------------------------
    // Parse SMS Messages
    // -----------------------------------------------------
    @PutMapping("/batch")
    public ResponseEntity<Void> batchUpdate(@RequestBody List<BatchUpdateTransactionRequestDto> requests) {
        batchService.batchConfirmAndUpdate(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"), requests);
        return ResponseEntity.ok().build();
    }
}
