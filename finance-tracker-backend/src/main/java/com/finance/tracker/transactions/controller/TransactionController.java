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
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<TransactionResponseDto> create(@Valid @RequestBody CreateTransactionRequestDto dto, Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.create(dto, userId));
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
            @RequestParam(name = "status", required = false, defaultValue = "CONFIRMED") String status,
            Authentication auth
    ) {
        PageRequest pageRequest =
                PageRequest.of(page - 1, size, Sort.by("occurredAt").descending());
        UUID userId = UUID.fromString((String) auth.getPrincipal());

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
    public ResponseEntity<?> delete(@PathVariable("id") UUID id, Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        return transactionService.getTransaction(id)
                .map(trx -> {
                    transactionService.deleteTransaction(userId, trx);
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
    @PostMapping("/analysis")
    public ResponseEntity<MonthlyExpenseResponse> getExpenseAnalysis(@RequestBody ExpenseReportRequest expenseReportRequest,  Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        MonthlyExpenseResponse response = transactionService.getExpenseReport(userId, expenseReportRequest);
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
    public ResponseEntity<ParsedTxnResponse> parse(@RequestBody SmsRequest message,  Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        return ResponseEntity.ok(transactionService.parse(userId, message));
    }

    // -----------------------------------------------------
    // Parse SMS Messages
    // -----------------------------------------------------
    @PutMapping("/batch")
    public ResponseEntity<Void> batchUpdate(@RequestBody List<BatchUpdateTransactionRequestDto> requests, Authentication auth) {
        UUID userId = UUID.fromString((String) auth.getPrincipal());
        batchService.batchConfirmAndUpdate(userId, requests);
        return ResponseEntity.ok().build();
    }

    // -----------------------------------------------------
    // Delete Draft Transactions
    // -----------------------------------------------------
    @PostMapping("/drafts/batch-delete")
    public ResponseEntity<Void> batchDelete(@RequestBody List<UUID> transactionIds) {
        if (transactionIds == null || transactionIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        batchService.deleteDraftTransactions(transactionIds);
        return ResponseEntity.noContent().build(); // 204 No Content is standard for successful deletes
    }
}
