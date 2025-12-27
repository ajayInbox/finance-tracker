package com.finance.tracker.transactions.service.impl;

import com.finance.tracker.transactions.domain.*;
import com.finance.tracker.transactions.domain.entities.Transaction;
import com.finance.tracker.transactions.repository.TransactionQueryBuilder;
import com.finance.tracker.transactions.repository.TransactionRepository;
import com.finance.tracker.transactions.service.TransactionAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class TransactionAnalyticsServiceImpl implements TransactionAnalyticsService {

    private final TransactionRepository transactionRepository;

    private static final DateTimeFormatter LOCAL_DATE_TIME_FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final ZoneId APP_ZONE_ID = ZoneId.of("Asia/Kolkata");

    @Override
    public TransactionsAverage search(SearchRequest searchRequest) {

        Instant now = Instant.now();
        Instant fromInstant = now.minus(Duration.ofDays(6));
        Instant toInstant = now;

        if (searchRequest.fromDate() != null && searchRequest.toDate() != null) {
            fromInstant = parseToInstant(searchRequest.fromDate());
            toInstant = parseToInstant(searchRequest.toDate());
        }

        Specification<Transaction> specs = Specification.allOf(
                TransactionQueryBuilder.occurredBetween(fromInstant, toInstant),
                TransactionQueryBuilder.type(TransactionType.EXPENSE)
        );

        List<Transaction> transactions = transactionRepository.findAll(specs);

        // convert instants to LocalDate in APP_ZONE_ID
        LocalDate fromDate = fromInstant.atZone(APP_ZONE_ID).toLocalDate();
        LocalDate toDate = toInstant.atZone(APP_ZONE_ID).toLocalDate();

        long days = Duration.between(fromDate.atStartOfDay(APP_ZONE_ID).toInstant(),
                                     toDate.plusDays(1).atStartOfDay(APP_ZONE_ID).toInstant())
                .toDays();
        int dayCount = Math.toIntExact(days);

        Map<LocalDate, BigDecimal> totalsByDate =
                transactions.stream()
                        .collect(Collectors.groupingBy(
                                t -> t.getOccurredAt()
                                        .atZone(APP_ZONE_ID)
                                        .toLocalDate(),
                                Collectors.reducing(
                                        BigDecimal.ZERO,
                                        Transaction::getAmount,
                                        BigDecimal::add
                                )
                        ));

        List<LocalDate> daysList = Stream.iterate(fromDate, d -> d.plusDays(1))
                .limit(dayCount)
                .toList();

        List<TransactionDaily> daily = daysList.stream()
                .map(d -> new TransactionDaily(
                        d,
                        totalsByDate.getOrDefault(d, BigDecimal.ZERO).doubleValue()
                ))
                .toList();

        BigDecimal sum = daily.stream()
                .map(t -> BigDecimal.valueOf(t.getTotalExpense()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal avg = dayCount == 0
                ? BigDecimal.ZERO
                : sum.divide(BigDecimal.valueOf(dayCount), 1, RoundingMode.HALF_UP);

        return TransactionsAverage.builder()
                .fromDate(LocalDateTime.from(fromInstant))
                .toDate(LocalDateTime.from(toInstant))
                .days(dayCount)
                .dailyList(daily)
                .averageDailyExpense(avg.doubleValue())
                .build();
    }

    @Override
    public MonthlyExpenseResponse getExpenseReport(ExpenseReportDuration duration) {

        Instant now = Instant.now();
        ZonedDateTime nowZdt = now.atZone(APP_ZONE_ID);

        DateRange range = resolveRange(duration, null, null);

        List<CategoryExpenseSummary> summaries =
                transactionRepository.findCategorySummary(null, range.start(), range.end());

        BigDecimal total = summaries.stream()
                .map(CategoryExpenseSummary::total)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CategoryBreakdown> breakdown = summaries.stream()
                .map(s -> {
                    BigDecimal subtotal = s.total();
                    BigDecimal percentage = total.compareTo(BigDecimal.ZERO) > 0
                            ? subtotal.divide(total, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(1, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return new CategoryBreakdown(
                            s.categoryId(),
                            s.categoryName(),
                            subtotal,
                            percentage,
                            Math.toIntExact(s.transactionCount())
                    );
                })
                .toList();

        return new MonthlyExpenseResponse(
                nowZdt.toLocalDate().toString(),
                "INR",
                total,
                breakdown
        );
    }

    private Instant parseToInstant(String value) {
        if (value == null) return null;
        // try Instant
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
        }
        // fallback local
        LocalDateTime ldt = LocalDateTime.parse(value, LOCAL_DATE_TIME_FORMATTER);
        return ldt.atZone(APP_ZONE_ID).toInstant();
    }

    private ZonedDateTime getFromZonedDateTime(ZonedDateTime nowZdt, ExpenseReportDuration duration) {
        return switch (duration) {
            case ExpenseReportDuration.THIS_MONTH -> nowZdt.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.LAST_MONTH -> nowZdt.minusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.LAST_3_MONTHS -> nowZdt.minusMonths(3).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.LAST_7_DAYS -> nowZdt.minusDays(7).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.LAST_30_DAYS -> nowZdt.minusDays(30).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.THIS_YEAR -> nowZdt.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
            case ExpenseReportDuration.LAST_YEAR -> nowZdt.minusYears(1).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
            case CUSTOM -> nowZdt.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
        };
    }

    private DateRange resolveRange(ExpenseReportDuration duration, Instant customFrom, Instant customTo) {

        ZonedDateTime now = ZonedDateTime.now(APP_ZONE_ID);

        return switch (duration) {

            case THIS_MONTH -> new DateRange(
                    now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant(),
                    now.toInstant()
            );

            case LAST_MONTH -> {
                ZonedDateTime start = now.minusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime end = start.plusMonths(1).minusNanos(1); // last moment of last month
                yield new DateRange(start.toInstant(), end.toInstant());
            }

            case LAST_7_DAYS -> new DateRange(
                    now.minusDays(7).toInstant(),
                    now.toInstant()
            );

            case LAST_30_DAYS -> new DateRange(
                    now.minusDays(30).toInstant(),
                    now.toInstant()
            );

            case THIS_YEAR -> new DateRange(
                    now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS).toInstant(),
                    now.toInstant()
            );

            case LAST_YEAR -> {
                ZonedDateTime start = now.minusYears(1).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime end = start.plusYears(1).minusNanos(1);
                yield new DateRange(start.toInstant(), end.toInstant());
            }
            case LAST_3_MONTHS -> {

                // last month (full)
                ZonedDateTime lastMonthStart = now.minusMonths(1).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);

                // start = first day of 3 months before last month
                ZonedDateTime start = lastMonthStart.minusMonths(2);

                // end = last day of last month (23:59:59.999999999)
                ZonedDateTime end = lastMonthStart.plusMonths(1).minusNanos(1);

                yield new DateRange(start.toInstant(), end.toInstant());
            }

            case CUSTOM -> new DateRange(customFrom, customTo);
        };
    }

}
