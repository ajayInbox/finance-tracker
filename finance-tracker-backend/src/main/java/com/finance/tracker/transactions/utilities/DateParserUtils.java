package com.finance.tracker.transactions.utilities;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class DateParserUtils {

    // Define the pattern to match the SMS: "dd-MM-yy HH:mm:ss"
    // Note: 'yy' is for 2-digit year (26), 'yyyy' would be for 2026.
    private static final DateTimeFormatter SMS_TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss", Locale.ENGLISH);

    public static LocalDateTime combine(String dateStr, String timeStr) {
        try {
            // 1. Concatenate them with a space
            String rawTimestamp = dateStr + " " + timeStr; 
            // Result: "10-01-26 08:50:53"

            // 2. Parse into a LocalDateTime object
            return LocalDateTime.parse(rawTimestamp, SMS_TIMESTAMP_FORMAT);
            
        } catch (DateTimeParseException e) {
            // Fallback: If parsing fails, use current system time
            // or log the error to alert you to a format change
            System.err.println("Failed to parse date: " + dateStr + " " + timeStr);
            return LocalDateTime.now(); 
        }
    }
}