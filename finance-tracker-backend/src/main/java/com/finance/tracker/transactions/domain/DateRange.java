package com.finance.tracker.transactions.domain;

import java.time.Instant;

public record DateRange(Instant start, Instant end) {}
