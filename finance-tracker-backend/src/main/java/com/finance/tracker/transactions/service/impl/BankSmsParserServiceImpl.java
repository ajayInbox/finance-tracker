package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.BankTemplate;
import com.finance.tracker.transactions.domain.ParsedTransaction;
import com.finance.tracker.transactions.domain.SmsMessage;
import com.finance.tracker.transactions.service.SmsParserService;
import com.finance.tracker.transactions.utilities.BankSenderRegistry;
import com.finance.tracker.transactions.utilities.DateParserUtils;
import com.finance.tracker.transactions.utilities.TemplateLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Service("bankSmsParser")
@RequiredArgsConstructor
@Slf4j
public class BankSmsParserServiceImpl implements SmsParserService {

    private final BankSenderRegistry senderRegistry;

    @Override
    public Optional<ParsedTransaction> parse(SmsMessage sms) {

        Optional<String> bankOpt = senderRegistry.resolveBank(sms.getMessageHeader());
        if (bankOpt.isEmpty()) {
            return Optional.empty();
        }

        String bank = bankOpt.get();
        List<BankTemplate> templates = TemplateLoader.load(bank);
        String smsBody = sms.getMessageBody();

        ParsedTransaction bestTx = null;
        double bestConfidence = 0.0;

        for (BankTemplate template : templates) {
            Matcher matcher = template.getCompiledPattern().matcher(smsBody);

            if (matcher.find()) {
                double confidence = calculateConfidence(template, matcher);
                log.info("Bank={} Template={} Confidence={}", bank, template.getName(), confidence);

                if (confidence > bestConfidence) {
                    bestConfidence = confidence;

                    bestTx = ParsedTransaction.builder()
                            .bank(bank)
                            .amount(get(matcher, "Amount"))
                            .merchant(get(matcher, "Merchant"))
                            .lastFour(extractLast4Digits(get(matcher, "LastFour")))
                            .dateTime(DateParserUtils.combine(get(matcher, "Date"), get(matcher, "Time")).toString())
                            .availableLimit(get(matcher, "AvailableLimit"))
                            .confidence(confidence)
                            .build();
                }
            }
        }

        return Optional.ofNullable(bestTx);
    }

    private String get (Matcher matcher, String group){
        try {
            return matcher.group(group);
        } catch (Exception e) {
            return null;
        }
    }

    private double calculateConfidence(BankTemplate template, Matcher matcher) {

        int totalFields = template.getFields().size();
        int matchedFields = 0;

        for (String field : template.getFields()) {
            try {
                String value = matcher.group(field);
                if (value != null && !value.isBlank()) {
                    matchedFields++;
                }
            } catch (IllegalArgumentException ignored) {
                // named group not present in regex
            }
        }

        return matchedFields / (double) totalFields;
    }

    public String extractLast4Digits(String accountStr) {
        if (accountStr == null) return "";

        // Remove anything that isn't a number
        String digitsOnly = accountStr.replaceAll("[^0-9]", "");

        // Now get the last 4 of the digits
        if (digitsOnly.length() <= 4) return digitsOnly;
        return digitsOnly.substring(digitsOnly.length() - 4);
    }

}
