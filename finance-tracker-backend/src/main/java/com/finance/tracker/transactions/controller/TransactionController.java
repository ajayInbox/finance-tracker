package com.finance.tracker.transactions.controller;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.dtos.TransactionCreateUpdateRequestDto;
import com.finance.tracker.transactions.domain.dtos.TransactionDto;
import com.finance.tracker.transactions.domain.dtos.TransactionsAverageDto;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@CrossOrigin
public class TransactionController {

    private final TransactionService transactionService;
    private final TransactionMapper transactionMapper;


    @PostMapping("/transaction")
    public ResponseEntity<TransactionDto> createTransaction(@RequestBody TransactionCreateUpdateRequestDto dto){
        TransactionCreateUpdateRequest request = transactionMapper.toTransactionCreateUpdateRequest(dto);
        Transaction createdTransaction = transactionService.createNewTransaction(request);
        return new ResponseEntity<>(transactionMapper.toDto(createdTransaction), HttpStatus.CREATED);
    }

    @GetMapping("/transaction/{id}")
    public ResponseEntity<TransactionDto> getTransaction(@PathVariable("id") String id) {
        return transactionService.getTransaction(id)
                .map(transaction -> ResponseEntity.ok(transactionMapper.toDto(transaction)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/transactions")
    public Object getTransactions(
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "size", defaultValue = "50", required = false) int size,
            @RequestParam(value = "version", defaultValue = "1", required = false) int version
    ){
        if(version==1){
            Page<Transaction> transactions = transactionService.getTransactions(PageRequest.of(page-1, size, Sort.by("occuredAt").descending()));
            return new ResponseEntity<>(transactions.map(transactionMapper::toDto), HttpStatus.OK);
        } else if (version==2) {
            Page<TransactionsWithCategoryAndAccount> transactions = transactionService.getTransactionsV2(PageRequest.of(page-1, size, Sort.by("occuredAt").descending()));
            return new ResponseEntity<>(transactions.map(transactionMapper::toDto), HttpStatus.OK);
        }
        return ResponseEntity.badRequest();
    }

    @PostMapping("/avg-daily-expense")
    public ResponseEntity<TransactionsAverageDto> search(@RequestBody SearchRequest searchRequest) {
        TransactionsAverage average = transactionService.search(searchRequest);
        return new ResponseEntity<>(transactionMapper.toDto(average), HttpStatus.OK);
    }

    @GetMapping("/avg-expense-analysis")
    public ResponseEntity<MonthlyExpenseResponse> getExpenseReport(
            @RequestParam(value = "duration", required = false) String duration
    ){
        MonthlyExpenseResponse monthlyExpenseResponse = transactionService.getExpenseReport(duration);
        return new ResponseEntity<>(monthlyExpenseResponse, HttpStatus.OK);
    }

    @PostMapping("/export-messages")
    public ResponseEntity<Void> exportMessages(@RequestBody List<SmsMessage> messageList){
        transactionService.exportMessagesSendToQueue(messageList);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/transaction/{id}")
    public ResponseEntity<String> deleteTransaction(@PathVariable("id") String transactionId){
        Optional<Transaction> transaction = transactionService.getTransaction(transactionId);
        if(transaction.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        String msg = transactionService.deleteTransaction(transaction.get());
        return ResponseEntity.ok(msg);
    }

    @PutMapping("/transaction/update")
    public ResponseEntity<TransactionDto> updateTransaction(@RequestBody TransactionCreateUpdateRequest request){
        Transaction updatedTransaction = transactionService.updateTransaction(request);
        return new ResponseEntity<>(transactionMapper.toDto(updatedTransaction), HttpStatus.OK);
    }
}
