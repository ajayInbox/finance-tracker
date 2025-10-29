package com.finance.tracker.transactions.utilities;

import java.util.List;
import java.util.regex.Pattern;

public class Regex {

    public static final List<Pattern> AXIS_PATTERNS = List.of(
            // 1) Axis Credit Card - "Spent" Format (Your SMS)
            Pattern.compile(
                    "(?s)" +                             // DOTALL: make . match newlines
                            "Spent\\s*" +
                            "Card no\\.\\s*XX(?<CardLast4>\\d{4})\\s*" +
                            "INR\\s*(?<Amount>[\\d,.]+)\\s*" +    // allow decimal & comma
                            "(?<DateTime>\\d{2}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})\\s*" +
                            "(?<Merchant>.+?)\\s*" +
                            "Avl Lmt\\s*INR\\s*(?<AvailableLimit>[\\d,.]+)"

                    ,
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            ),
            // 2) Axis Credit Card - "Spent" Format (Your SMS)
            Pattern.compile(
                    "(?s)" +                                   // DOTALL: . matches newlines
                            "Spent\\s*INR\\s*(?<Amount>[\\d,.]+)\\s*" + // Spent INR 268.65
                            ".*?Card no\\.\\s*XX(?<CardLast4>\\d{4})\\s*" + // Card number
                            "(?<DateTime>\\d{2}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2})(?:\\s*IST)?\\s*" + // Date + optional IST
                            "(?<Merchant>[A-Za-z0-9 &.,-]+)\\s*" +     // Merchant name
                            "Avl\\s*L(?:imit|mt):?\\s*INR\\s*(?<AvailableLimit>[\\d,.]+)",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            )

    );

    public static final List<Pattern> SBI_PATTERNS = List.of();
    public static final List<Pattern> KOTAK_PATTERNS = List.of();
    public static final List<Pattern> ICICI_PATTERNS = List.of();
    public static final List<Pattern> CBI_PATTERNS = List.of();
}
