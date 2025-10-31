package com.finance.tracker.transactions.utilities;

import com.finance.tracker.transactions.domain.BankTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.*;

@Component
public class SmsParser {
    public Map<String, String> parse(String bankName, String sms) {
        List<BankTemplate> templates = TemplateLoader.load(bankName);
        if (templates == null) return null;

        for (BankTemplate template : templates) {
            Pattern pattern = Pattern.compile(template.getPattern(), Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(sms);

            if (matcher.find()) {
                Map<String, String> map = new HashMap<>();

                for (String field : template.getFields()) {
                    try {
                        String value = matcher.group(field);
                        if (value == null) continue;
                        switch (field) {
                            case "CardLast4" -> map.put("CardLast4", value);
                            case "Amount" -> map.put("Amount", value);
                            case "DateTime" -> map.put("DateTime", value);
                            case "Merchant" -> map.put("Merchant", value);
                            case "AvailableLimit" -> map.put("AvailableLimit", value);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }

                return map; // success
            }
        }
        return null; // no match found
    }
}
