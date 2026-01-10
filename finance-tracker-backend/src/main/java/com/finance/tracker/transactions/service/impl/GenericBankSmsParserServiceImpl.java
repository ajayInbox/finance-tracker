package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsMessage;
import com.finance.tracker.transactions.service.SmsParserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("genericSmsParser")
public class GenericBankSmsParserServiceImpl implements SmsParserService {

    private static final Pattern AMOUNT =
            Pattern.compile("(?i)(rs\\.?|inr|₹)\\s?([\\d,]+(?:\\.\\d{1,2})?)");

    private static final Pattern DEBIT =
            Pattern.compile("(?i)(debited|spent|paid|withdrawn|purchase)");

    private static final Pattern CREDIT =
            Pattern.compile("(?i)(credited|received|refund|deposited)");

    private static final Pattern MERCHANT =
            Pattern.compile("(?i)(?:at|to)\\s+([a-z0-9 &._-]{3,})");

    private static final Pattern DATE =
            Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");

    private static final Pattern LAST_FOUR =
            Pattern.compile("(?i)(?:AC|A/C|A/c|a/c|ac)\\s*[Xx]?(\\d{4})\\b");

    @Override
    public Optional<ParsedTransaction> parse(SmsMessage sms) {
        String smsBody = sms.getMessageBody();
        String amount = extract(AMOUNT, smsBody, 2);
        if (amount == null) {
            return Optional.empty(); // amount is mandatory
        }

        String type = DEBIT.matcher(smsBody).find() ? "DEBIT"
                : CREDIT.matcher(smsBody).find() ? "CREDIT"
                : null;

        String merchant = extract(MERCHANT, smsBody, 1);
        String date = extract(DATE, smsBody, 1);
        String lastFour = extract(LAST_FOUR, smsBody, 1);

        double confidence = calculateConfidence(amount, type, merchant, date);

        return Optional.of(
                ParsedTransaction.builder()
                        .bank("UNKNOWN")
                        .amount(amount)
                        .lastFour(lastFour)
                        .merchant(merchant != null ? merchant.toUpperCase() : null)
                        .dateTime(date)
                        .confidence(confidence)
                        .build()
        );
    }

    private String extract(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group) : null;
    }

    private double calculateConfidence(String amount, String type,
                                       String merchant, String date) {

        int score = 0;
        if (amount != null) score += 50;
        if (type != null) score += 20;
        if (merchant != null) score += 20;
        if (date != null) score += 10;

        return score / 100.0;
    }
}
