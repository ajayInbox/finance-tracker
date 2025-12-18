package com.finance.tracker.accounts.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class DateTimeMapper {
    
    // private static final ZoneId DEFAULT_ZONE = ZoneId.of("Asia/Kolkata");
    // Or use UTC if your app assumes UTC everywhere:
    private static final ZoneId DEFAULT_ZONE = ZoneOffset.UTC;

    private DateTimeMapper() {}

    // ---------------- REQUEST ----------------
    // LocalDateTime (from user) → Instant (for DB)
    public static Instant toInstant(LocalDateTime localDateTime) {
        return localDateTime == null
                ? null
                : localDateTime.atZone(DEFAULT_ZONE).toInstant();
    }

    // ---------------- RESPONSE ----------------
    // Instant (from DB) → LocalDateTime (for user)
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return instant == null
                ? null
                : LocalDateTime.ofInstant(instant, DEFAULT_ZONE);
    }

}
