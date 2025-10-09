package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.exceptions.CurrencyMismatchException;
import com.finance.tracker.transactions.repository.TransactionQueryBuilder;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final ApplicationEventPublisher eventPublisher;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

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

    @Override
    public TransactionsAverage search(SearchRequest searchRequest) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(6);
        LocalDateTime toDate = LocalDateTime.now();
        if(searchRequest.fromDate()!=null && searchRequest.toDate()!=null){
            fromDate = LocalDateTime.parse(searchRequest.fromDate(), FORMATTER);
            toDate = LocalDateTime.parse(searchRequest.toDate(), FORMATTER);
        }

        Specification<Transaction> specs = Specification.where(
                TransactionQueryBuilder.occurredBetween(fromDate, toDate, ZoneOffset.UTC)
        ).and(TransactionQueryBuilder.type(TransactionType.EXPENSE));
        List<Transaction> transactions = transactionRepository.findAll(specs);

        long days = Duration.between(fromDate, toDate).toDays();
        int dayCount = Math.toIntExact(days);

        Map<LocalDate, BigDecimal> totalsByDate =
                transactions.stream()
                        .map(t -> Map.entry(t, t.getOccuredAt().toLocalDate()))
                        .collect(Collectors.groupingBy(
                                e -> e.getKey().getOccuredAt().toLocalDate(),
                                Collectors.reducing(BigDecimal.ZERO,
                                        e -> BigDecimal.valueOf(e.getKey().getAmount()),  // map each entry to its amount
                                        BigDecimal::add)
                        ));


        List<LocalDate> daysList = Stream.iterate(fromDate, d -> d.plusDays(1))
                .map(LocalDateTime::toLocalDate)
                .limit(dayCount)
                .toList();

        List<TransactionDaily> daily = daysList.stream()
                .map(d -> new TransactionDaily(d, totalsByDate.getOrDefault(d, BigDecimal.ZERO).doubleValue()))
                .toList();

        BigDecimal sum = daily.stream()
                .map(t -> BigDecimal.valueOf(t.getTotalExpense()))
                .reduce(BigDecimal.ZERO, BigDecimal::add); // [web:99]

        BigDecimal avg = dayCount == 0
                ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(dayCount), 1, RoundingMode.HALF_UP);
        return TransactionsAverage.builder()
                .fromDate(fromDate)
                .toDate(toDate)
                .days(dayCount)
                .dailyList(daily)
                .averageDailyExpense(avg.doubleValue())
                .build();
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
