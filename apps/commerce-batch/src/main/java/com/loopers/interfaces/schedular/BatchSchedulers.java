package com.loopers.interfaces.schedular;

import com.loopers.domain.ranking.ProductMetricsMonthly;
import com.loopers.domain.ranking.ProductMetricsWeekly;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.StringJoiner;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchSchedulers {

    private final JobLauncher jobLauncher;
    private final Job weeklyRankingJob;
    private final Job monthlyRankingJob;

    @Scheduled(cron = "0 5 1 * * *")   // 매일 01:05
    public void runWeekly() throws Exception {
        LocalDate today = LocalDate.now();
        var yw = ProductMetricsWeekly.YearWeek.of(today);
        var start = ProductMetricsWeekly.YearWeek.start(today);
        var end = ProductMetricsWeekly.YearWeek.end(today);

        JobParameters params = new JobParametersBuilder()
                .addString("yearWeek", yw.value())
                .addString("start", start.toString())
                .addString("end", end.toString())
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(weeklyRankingJob, params);
    }

    @Scheduled(cron = "0 20 1 * * *")  // 매일 01:20
    public void runMonthly() throws Exception {
        LocalDate today = LocalDate.now();
        var ym = ProductMetricsMonthly.YearMonth.of(today);

        LocalDate first = today.withDayOfMonth(1);
        LocalDate last  = today.withDayOfMonth(today.lengthOfMonth());
        StringJoiner weeks = new StringJoiner(",");
        LocalDate cur = first;
        while(!cur.isAfter(last)){
            weeks.add(ProductMetricsWeekly.YearWeek.of(cur).value());
            cur = cur.plusDays(1);
        }

        JobParameters params = new JobParametersBuilder()
                .addString("yearMonth", ym.value())
                .addString("weeksCsv", weeks.toString())
                .addLong("ts", System.currentTimeMillis())
                .toJobParameters();

        jobLauncher.run(monthlyRankingJob, params);
    }
}
