package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.TransactionCreateEvent;
import com.finance.tracker.transactions.domain.TransactionCreateUpdateRequest;
import com.finance.tracker.transactions.domain.TransactionType;
import com.finance.tracker.transactions.domain.TransactionsWithCategoryAndAccount;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.exceptions.CurrencyMismatchException;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Transaction createNewTransaction(TransactionCreateUpdateRequest request) {

        validateTransactionRequest(request);

        Transaction transaction = saveTransaction(request);
        eventPublisher.publishEvent(new TransactionCreateEvent(this, transaction.getId(),
                transaction.getAccount(), transaction.getAmount(), transaction.getType(), transaction.getOccuredAt()));

        return transaction;
    }

    @Override
    public Optional<Transaction> getTransaction(String id) {
        return transactionRepository.findById(id);
    }

    @Override
    public Page<Transaction> getTransactions(Pageable pageable) {
        return transactionRepository.findAll(pageable);
    }

    @Override
    public Page<TransactionsWithCategoryAndAccount> getTransactionsV2(Pageable pageable) {
        return transactionRepository.fetchTransactions(pageable);
    }

    private Transaction saveTransaction( TransactionCreateUpdateRequest request){
        Transaction newTransaction = Transaction.builder()
                .type(TransactionType.fromValueIgnoreCase(request.type()))
                .transactionName(request.transactionName())
                .currency(request.currency())
                .account(request.account())
                .amount(request.amount())
                .category(request.category())
                .merchant(request.merchant())
                .occuredAt(LocalDateTime.parse(request.occuredAt(), DateTimeFormatter.ISO_DATE_TIME))
                .postedAt(LocalDateTime.parse(request.occuredAt(), DateTimeFormatter.ISO_DATE_TIME))
                .notes(request.notes())
                .attachments(request.attachments())
                .externalRef(request.externalRef())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        return transactionRepository.save(newTransaction);
    }

    private void validateTransactionRequest(TransactionCreateUpdateRequest request){
        // TODO fix this
        String userId = null;

        Account account = accountService.getAccountByIdAndUser(request.account(), userId);
        if(!account.getCurrency().equalsIgnoreCase("INR")){
            throw new CurrencyMismatchException();
        }

        categoryService.validateCategoryForTransaction(userId, request.category(),
                TransactionType.fromValueIgnoreCase(request.type()));
    }
}
