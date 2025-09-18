package com.loopers.domain.rank;

import lombok.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;

@Getter
@ToString
@EqualsAndHashCode(of = "productId")
@AllArgsConstructor
@Builder
public class Rank {
    private final Long productId;
    private final Long position;
    private final double score;

    private static final DateTimeFormatter D_YYYYMMDD = DateTimeFormatter.BASIC_ISO_DATE;

    public static Rank create(Long productId, Long position, double score) {
        return new Rank(productId, position, score);
    }

    public static String buildKey(String periodRaw, LocalDate date,
                                  String dailyPrefix, String weeklyPrefix, String monthlyPrefix) {
        final String period = (periodRaw == null ? "daily" : periodRaw.toLowerCase());

        return switch (period) {
            case "weekly" -> weeklyPrefix + ":" + toYearWeek(date);
            case "monthly" -> monthlyPrefix + ":" + toYearMonth(date);
            default -> dailyPrefix  + ":" + date.format(D_YYYYMMDD);
        };
    }

    private static String toYearWeek(LocalDate date) {
        var wf = WeekFields.ISO;
        int w = date.get(wf.weekOfWeekBasedYear());
        int y = date.get(wf.weekBasedYear());
        return "%04dW%02d".formatted(y, w);
    }

    private static String toYearMonth(LocalDate date) {
        return DateTimeFormatter.ofPattern("yyyyMM").format(date);
    }
}
