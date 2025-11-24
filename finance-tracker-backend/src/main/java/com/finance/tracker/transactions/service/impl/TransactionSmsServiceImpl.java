package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.accounts.service.AccountService;
import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.SmsMessage;
import com.finance.tracker.transactions.domain.TransactionCreateUpdateRequest;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.mapper.TransactionMapper;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.MessageProducer;
import com.finance.tracker.transactions.service.TransactionSmsService;
import com.finance.tracker.transactions.utilities.SmsParser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TransactionSmsServiceImpl implements TransactionSmsService {

    private final SmsParser smsParser;
    private final MessageProducer messageProducer;
    private final AccountService accountService;
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;

    @Value("${transaction.default-category-id}")
    private String defaultCategoryId;

    private static final DateTimeFormatter SMS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    // Choose your app zone
    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Kolkata");

    @Override
    public void exportMessages(List<SmsMessage> messageList) {
        for (SmsMessage message : messageList) {
            createTransactionFromMessage(message);
        }
    }

    @Override
    public void exportMessagesSendToQueue(List<SmsMessage> messageList) {
        for (SmsMessage message : messageList) {
            messageProducer.sendMessage(message);
        }
    }

    @Override
    public void createTransactionFromQueueMsg(SmsMessage message) {
        createTransactionFromMessage(message);
    }

    private void createTransactionFromMessage(SmsMessage message) {
        String bank = checkBank(message.getMessageHeader());
        Map<String, String> parsedObject = smsParser.parse(bank, message.getMessageBody());

        String accountId = accountService.getAccountByLastFour(parsedObject.get("CardLast4"));
        parsedObject.put("accountId", accountId);

        CreateTransactionRequest request = buildTransactionCreateRequest(parsedObject);
        // TODO: user id from SMS context if you support multi-user; for now null
        String userId = null;
        Transaction transaction = transactionMapper.toNewEntity(request, userId);
        transactionRepository.save(transaction);
    }

    private String checkBank(String header) {
        // You can move the banks list here or inject
        List<String> banks = List.of("HDFC", "SBI", "ICICI", "Axis");
        for (String bank : banks) {
            if (header.toUpperCase().contains(bank.toUpperCase())) {
                return bank.toUpperCase();
            }
        }
        // TODO: if not able to get bank then think how we can identify bank name for patterns
        return null;
    }

    private CreateTransactionRequest buildTransactionCreateRequest(Map<String, String> object) {
        LocalDateTime localDateTime =
                LocalDateTime.parse(object.get("DateTime"), SMS_DATE_FORMATTER);
        Instant when = localDateTime.atZone(APP_ZONE_ID).toInstant();

        return CreateTransactionRequest.builder()
                .transactionName("New Expense Transaction")
                .merchant(object.get("Merchant"))
                .currency("INR")
                .account(object.get("accountId")) // use the lower-case key we inserted
                .amount(BigDecimal.valueOf(Long.parseLong(object.get("Amount"))))
                .type("expense")
                .attachments("")
                .tags(List.of())
                .notes("")
                .occurredAt(when.toString())
                .postedAt(when.toString())
                .category(defaultCategoryId)
                .build();
    }
}
