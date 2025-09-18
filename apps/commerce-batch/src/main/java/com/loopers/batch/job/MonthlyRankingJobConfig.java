package com.loopers.batch.job;

import com.loopers.domain.ranking.AggregatedOut;
import com.loopers.infrastructure.ranking.RedisRankingWriter;
import com.loopers.domain.ranking.ScoreRow;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class MonthlyRankingJobConfig {

    private final JobRepository repo;
    private final PlatformTransactionManager tx;

    @Bean
    public Job monthlyRankingJob(Step monthlyStep) {
        return new JobBuilder("monthlyRankingJob", repo)
                .start(monthlyStep)
                .build();
    }

    @Bean
    public Step monthlyStep(
            ItemReader<ScoreRow> monthlyFromWeeklyReader,
            ItemProcessor<ScoreRow, AggregatedOut> monthlyProcessor,
            JdbcBatchItemWriter<AggregatedOut> monthlyUpsertWriter,
            RedisRankingWriter redisWriter
    ){
        return new StepBuilder("monthlyStep", repo)
                .<ScoreRow, AggregatedOut>chunk(1000, tx)
                .reader(monthlyFromWeeklyReader)
                .processor(monthlyProcessor)
                .writer(items -> {
                    monthlyUpsertWriter.write(items);
                    redisWriter.write(items);
                })
                .build();
    }
}
