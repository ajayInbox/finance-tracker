package com.finance.tracker.transactions.utilities;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class DateParserUtils {

    public static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Kolkata");

    private static final DateTimeFormatter SMS_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss", Locale.ENGLISH);

    private static final List<DateTimeFormatter> DATE_TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_OFFSET_DATE_TIME,
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("dd-MM-yy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH)
    );

    private static final List<DateTimeFormatter> DATE_ONLY_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd-MM-yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd-MM-yyyy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yy", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH)
    );

    public static LocalDateTime combine(String dateStr, String timeStr) {
        if (dateStr == null || timeStr == null) {
            return LocalDateTime.now(APP_ZONE_ID);
        }
        try {
            String rawTimestamp = dateStr.trim() + " " + timeStr.trim();
            return LocalDateTime.parse(rawTimestamp, SMS_TIMESTAMP_FORMAT);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now(APP_ZONE_ID);
        }
    }

    public static OffsetDateTime parseFlexible(String rawDate, Long fallbackTimestampMillis) {
        if (rawDate != null && !rawDate.isBlank()) {
            String trimmed = rawDate.trim();

            // 1. Try ISO OffsetDateTime
            try {
                return OffsetDateTime.parse(trimmed);
            } catch (Exception ignored) {}

            // 2. Try Date-Time patterns
            for (DateTimeFormatter dtf : DATE_TIME_FORMATTERS) {
                try {
                    LocalDateTime ldt = LocalDateTime.parse(trimmed, dtf);
                    return ldt.atZone(APP_ZONE_ID).toOffsetDateTime();
                } catch (Exception ignored) {}
            }

            // 3. Try Date-Only patterns
            for (DateTimeFormatter dtf : DATE_ONLY_FORMATTERS) {
                try {
                    LocalDate ld = LocalDate.parse(trimmed, dtf);
                    return ld.atStartOfDay(APP_ZONE_ID).toOffsetDateTime();
                } catch (Exception ignored) {}
            }
        }

        // 4. Fallback to epoch milliseconds from SMS
        if (fallbackTimestampMillis != null && fallbackTimestampMillis > 0) {
            try {
                return Instant.ofEpochMilli(fallbackTimestampMillis)
                        .atZone(APP_ZONE_ID)
                        .toOffsetDateTime();
            } catch (Exception ignored) {}
        }

        // 5. Ultimate fallback
        return OffsetDateTime.now(APP_ZONE_ID);
    }
}
