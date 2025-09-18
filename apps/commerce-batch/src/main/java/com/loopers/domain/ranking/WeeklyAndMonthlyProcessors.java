package com.loopers.domain.ranking;

import com.loopers.support.config.ranking.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class WeeklyAndMonthlyProcessors {

    private final RankingProperties props;
    private final ProductMetricsMonthlyRepository productMetricsMonthlyRepository;
    private final ProductMetricsWeeklyRepository productMetricsWeeklyRepository;

    @Bean
    @StepScope
    public ItemProcessor<ScoreRow, AggregatedOut> weeklyProcessor(
            @Value("#{jobParameters['yearWeek']}") String yearWeek
    ){
        return row -> {
            double base = props.weight().view()  * row.viewCnt()
                          + props.weight().like()  * row.likeCnt()
                          + props.weight().sales() * row.salesCnt();

            double carry = 0.0;
            if (props.carryOverAlpha() > 0.0) {
                var thisWeek = new ProductMetricsWeekly.YearWeek(yearWeek);
                var prevWeek = thisWeek.prev();
                Optional<ProductMetricsWeekly> prev = productMetricsWeeklyRepository.findById(
                        new ProductMetricsWeeklyId(prevWeek.value(), row.productId()));
                if (prev.isPresent()) carry = prev.get().getScore() * props.carryOverAlpha();
            }

            double finalScore = Math.max(0.0, base + carry);
            String redisKey = props.weeklyPrefix() + ":" + yearWeek;

            return new AggregatedOut(yearWeek, row.productId(), finalScore, redisKey, String.valueOf(row.productId()));
        };
    }

    @Bean @StepScope
    public ItemProcessor<ScoreRow, AggregatedOut> monthlyProcessor(
            @Value("#{jobParameters['yearMonth']}") String yearMonth
    ){
        return row -> {
            double base = row.score();
            double carry = 0.0;

            if (props.carryOverAlpha() > 0.0) {
                var thisMonth = new ProductMetricsMonthly.YearMonth(yearMonth);
                var prevMonth = thisMonth.prev();
                Optional<ProductMetricsMonthly> prev = productMetricsMonthlyRepository.findById(
                        new ProductMetricsMonthlyId(prevMonth.value(), row.productId()));
                if (prev.isPresent()) carry = prev.get().getScore() * props.carryOverAlpha();
            }

            double finalScore = Math.max(0.0, base + carry);
            String redisKey = props.monthlyPrefix() + ":" + yearMonth;

            return new AggregatedOut(yearMonth, row.productId(), finalScore, redisKey, String.valueOf(row.productId()));
        };
    }
}
