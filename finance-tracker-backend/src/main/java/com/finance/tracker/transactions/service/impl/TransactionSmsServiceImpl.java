package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.CreateTransactionRequest;
import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsRequest;
import com.finance.tracker.transactions.exceptions.SmsParsingFailedException;
import com.finance.tracker.transactions.service.MessageProducer;
import com.finance.tracker.transactions.service.SmsParserService;
import com.finance.tracker.transactions.service.TransactionSmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionSmsServiceImpl implements TransactionSmsService {

    private final Map<String, SmsParserService> parserMap;
    private final MessageProducer messageProducer;

    @Value("${transaction.default-category-id}")
    private String defaultCategoryId;

    private static final DateTimeFormatter SMS_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss");

    // Choose your app zone
    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Kolkata");
    private static final double CONFIDENCE_THRESHOLD = 0.7;

    @Override
    public void exportMessages(List<SmsRequest> messageList) {
        for (SmsRequest message : messageList) {
          //  createTransactionFromMessage(message);
        }
    }

    @Override
    public void exportMessagesSendToQueue(List<SmsRequest> messageList) {
        for (SmsRequest message : messageList) {
            messageProducer.sendMessage(message);
        }
    }

    @Override
    public Optional<ParsedTransaction> parseTransactionFromSms(SmsRequest message) {

        //UPI → UPI parser
        if (isUpi(message.getBody())) {
            Optional<ParsedTransaction> upiResult =
                    parserMap.get("upiSmsParser").parse(message);

            if (isSatisfactory(upiResult)) {
                return upiResult;
            }
        }

        //Bank → Bank parser
        Optional<ParsedTransaction> bankResult =
                parserMap.get("bankSmsParser").parse(message);

        if (isSatisfactory(bankResult)) {
            return bankResult;
        }

        //Generic fallback
        Optional<ParsedTransaction> genericResult =
                parserMap.get("genericSmsParser").parse(message);

        if (isSatisfactory(genericResult)) {
            return genericResult;
        }

        return Optional.empty();
    }

    private boolean isSatisfactory(Optional<ParsedTransaction> result) {
        return result.isPresent()
                && result.get().getConfidence() >= CONFIDENCE_THRESHOLD;
    }

    private boolean isUpi(String sms) {
        return sms.matches("(?i).*\\bupi\\b.*|.*@.*");
    }
}
