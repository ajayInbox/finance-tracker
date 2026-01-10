package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsMessage;
import com.finance.tracker.transactions.service.SmsParserService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service("upiSmsParser")
public class UpiSmsParserServiceImpl implements SmsParserService {

    private static final Pattern UPI_HINT =
            Pattern.compile("(?i)\\bupi\\b|@");

    private static final Pattern AMOUNT =
            Pattern.compile("(?i)(rs\\.?|inr|₹)\\s?([\\d,]+(?:\\.\\d{1,2})?)");

    private static final Pattern DEBIT =
            Pattern.compile("(?i)(paid|sent|debited)");

    private static final Pattern CREDIT =
            Pattern.compile("(?i)(received|credited)");

    private static final Pattern UPI_ID =
            Pattern.compile("(?i)([a-z0-9._-]+@[a-z0-9._-]+)");

    private static final Pattern MERCHANT =
            Pattern.compile("(?i)(?:to|from)\\s+([a-z0-9._-]{3,})@");

    private static final Pattern UTR =
            Pattern.compile("(?i)(utr|ref)[^a-z0-9]?[:#]?\\s?([a-z0-9]+)");

    private static final Pattern DATE =
            Pattern.compile("(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})");

    private static final Pattern LAST_FOUR =
            Pattern.compile("(?i)(?:AC|A/C|A/c|a/c|ac)\\s*[Xx]?(\\d{4})\\b");

    @Override
    public Optional<ParsedTransaction> parse(SmsMessage sms) {
        // Fast reject if message doesn't look like UPI
        String smsBody = sms.getMessageBody();
        if (!UPI_HINT.matcher(smsBody).find()) {
            return Optional.empty();
        }

        String amount = extract(AMOUNT, smsBody, 2);
        if (amount == null) {
            return Optional.empty();
        }

        String type =
                DEBIT.matcher(smsBody).find() ? "DEBIT" :
                        CREDIT.matcher(smsBody).find() ? "CREDIT" :
                                null;

        String upiId = extract(UPI_ID, smsBody, 1);
        String merchant = extract(MERCHANT, smsBody, 1);
        String utr = extract(UTR, smsBody, 2);
        String date = extract(DATE, smsBody, 1);
        String lastFour = extract(LAST_FOUR, smsBody, 1);

        double confidence = calculateConfidence(amount, type, upiId, utr);

        return Optional.of(
                ParsedTransaction.builder()
                        .bank("UPI")
                        .amount(amount)
                        .lastFour(lastFour)
                        .merchant(
                                merchant != null
                                        ? merchant.toUpperCase()
                                        : extractMerchantFromUpi(upiId)
                        )
                        .referenceId(utr)
                        .dateTime(date)
                        .confidence(confidence)
                        .build()
        );
    }

    private String extract(Pattern pattern, String text, int group) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(group) : null;
    }

    private String extractMerchantFromUpi(String upiId) {
        if (upiId == null) return null;
        return upiId.split("@")[0].toUpperCase();
    }

    private double calculateConfidence(String amount, String type,
                                       String upiId, String utr) {

        int score = 0;
        if (amount != null) score += 40;
        if (type != null) score += 20;
        if (upiId != null) score += 20;
        if (utr != null) score += 20;

        return score / 100.0;
    }
}
