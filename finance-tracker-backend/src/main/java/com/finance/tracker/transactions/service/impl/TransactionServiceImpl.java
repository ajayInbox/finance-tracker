package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.domain.entities.Account;
import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.exceptions.CurrencyMismatchException;
import com.finance.tracker.transactions.repository.TransactionQueryBuilder;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.MessageProducer;
import com.finance.tracker.transactions.service.TransactionService;
import com.finance.tracker.transactions.utilities.SmsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    private final static List<String> banks = Arrays.asList("HDFC", "SBI", "ICICI", "Axis");
    private final SmsParser smsParser;
    private final MessageProducer messageProducer;
    @Value("${transaction.default-category-id}")
    private String defaultCategoryId;

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

    @Override
    public MonthlyExpenseResponse getExpenseReport(String duration) {
        LocalDateTime toDate = LocalDateTime.now();
        LocalDateTime fromDate = getFromDate(toDate, duration);

        Instant start = fromDate.toInstant(ZoneOffset.UTC);
        Instant end = toDate.toInstant(ZoneOffset.UTC);

        List<CategoryExpenseSummary> summaries = transactionRepository.findCategorySummary(null, start, end);

        BigDecimal total = summaries.stream()
                .map(s -> BigDecimal.valueOf(s.total()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdown> breakdown = summaries.stream()
                .map(s -> new CategoryBreakdown(
                        s.categoryId(),
                        s.categoryName(),
                        BigDecimal.valueOf(s.total()),
                        total.compareTo(BigDecimal.ZERO) > 0
                                ? BigDecimal.valueOf(s.total()).divide(total, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100)).setScale(1, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO,
                        Math.toIntExact(s.transactionCount())
                ))
                .toList();

        return new MonthlyExpenseResponse(toDate.toLocalDate().toString(), "INR", total, breakdown);
    }

    @Override
    public void exportMessages(List<SmsMessage> messageList) {
        System.out.println(messageList);
        for(SmsMessage message : messageList){
            createTransactionFromMessage(message);
        }
    }

    @Override
    public void exportMessagesSendToQueue(List<SmsMessage> messageList) {
        for(SmsMessage message : messageList){
            messageProducer.sendMessage(message);
        }
    }

    @Override
    public void createTransactionFromQueueMsg(SmsMessage message) {
        createTransactionFromMessage(message);
    }

    private void createTransactionFromMessage(SmsMessage message){
        String bank = checkBank(message.getMessageHeader());
        Map<String, String> parsedObject = smsParser.parse(bank, message.getMessageBody());
        String accountId = accountService.getAccountByLastFour(parsedObject.get("CardLast4"));
        parsedObject.put("accountId", accountId);
        TransactionCreateUpdateRequest request = buildTransactionCreateUpdateRequest(parsedObject);
        Transaction transaction = buildTransactionEntity(request);
        transactionRepository.save(transaction);
    }

    private Transaction saveTransaction( TransactionCreateUpdateRequest request){
        Transaction newTransaction = buildTransactionEntity(request);
        return transactionRepository.save(newTransaction);
    }

    private void validateTransactionRequest(TransactionCreateUpdateRequest request){
        // TODO fix this
        String userId = null;

        Account account = accountService.getAccountByIdAndUser(request.getAccount(), userId);
        if(!account.getCurrency().equalsIgnoreCase("INR")){
            throw new CurrencyMismatchException();
        }

        categoryService.validateCategoryForTransaction(userId, request.getCategory(),
                TransactionType.fromValueIgnoreCase(request.getType()));
    }

    private LocalDateTime getFromDate(LocalDateTime toDate, String duration){
        LocalDateTime fromDate = LocalDateTime.now();
        if("weekly".equalsIgnoreCase(duration)){
            fromDate = LocalDateTime.now().minusDays(6);
        } else if("yearly".equalsIgnoreCase(duration)){
            int year = toDate.getYear();
            fromDate = LocalDateTime.of(LocalDate.of(year, 1, 1), LocalTime.MIN);
        }else{
            int month = toDate.getMonthValue();
            int year = toDate.getYear();
            fromDate = LocalDateTime.of(LocalDate.of(year, month, 1), LocalTime.MIN);
        }
        return fromDate;
    }

    private String checkBank(String header){
        for(String bank : banks){

            if(header.toUpperCase().contains(bank.toUpperCase())){
                return bank.toUpperCase();
            }
        }
        //TODO if not able to get bank than need to think how we can identify bank name for patterns
        return null;
    }

    private Transaction buildTransactionEntity(TransactionCreateUpdateRequest request) {
        return Transaction.builder()
                .type(TransactionType.fromValueIgnoreCase(request.getType()))
                .transactionName(request.getTransactionName())
                .currency(request.getCurrency())
                .account(request.getAccount())
                .amount(request.getAmount())
                .category(request.getCategory())
                .merchant(request.getMerchant())
                .occuredAt(LocalDateTime.parse(request.getOccuredAt(), DateTimeFormatter.ISO_DATE_TIME))
                .postedAt(LocalDateTime.parse(request.getOccuredAt(), DateTimeFormatter.ISO_DATE_TIME))
                .notes(request.getNotes())
                .attachments(request.getAttachments())
                .externalRef(request.getExternalRef())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TransactionCreateUpdateRequest buildTransactionCreateUpdateRequest(Map<String, String> object){
        return TransactionCreateUpdateRequest.builder()
                .transactionName("New Expense Transaction")
                .merchant(object.get("Merchant"))
                .currency("INR")
                .account(String.valueOf(object.get("AccountId")))
                .amount(Double.valueOf(object.get("Amount")))
                .type("expense")
                .attachments("")
                .tags(List.of())
                .notes("")
                .occuredAt(String.valueOf(LocalDateTime.parse(object.get("DateTime"), DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"))))
                .postedAt(String.valueOf(LocalDateTime.parse(object.get("DateTime"), DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss"))))
                .category(defaultCategoryId)
                .build();
    }
}
