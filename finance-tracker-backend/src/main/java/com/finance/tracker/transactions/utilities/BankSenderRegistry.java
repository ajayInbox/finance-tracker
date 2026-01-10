package com.finance.tracker.transactions.utilities;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class BankSenderRegistry {

    private static final Map<String, String> SENDER_TO_BANK = Map.ofEntries(
            Map.entry("HDFC", "HDFC"),
            Map.entry("HDFCBK", "HDFC"),
            Map.entry("SBI", "SBI"),
            Map.entry("SBIINB", "SBI"),
            Map.entry("ICICI", "ICICI"),
            Map.entry("ICICIB", "ICICI"),
            Map.entry("AXIS", "AXIS"),
            Map.entry("AXISBK", "AXIS"),
            Map.entry("KOTAK", "KOTAK"),
            Map.entry("KOTAKB", "KOTAK"),
            Map.entry("PNB", "PNB"),
            Map.entry("YESBANK", "YES"),
            Map.entry("IDFC", "IDFC"),
            Map.entry("BOB", "BOB")
    );

    public Optional<String> resolveBank(String senderId) {
        if (senderId == null) return Optional.empty();

        String normalized = senderId.toUpperCase();

        return SENDER_TO_BANK.entrySet().stream()
                .filter(e -> normalized.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }
}
