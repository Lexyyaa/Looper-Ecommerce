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
public class WeeklyRankingJobConfig {

    private final JobRepository repo;
    private final PlatformTransactionManager tx;

    @Bean
    public Job weeklyRankingJob(Step weeklyStep) {
        return new JobBuilder("weeklyRankingJob", repo)
                .start(weeklyStep)
                .build();
    }

    @Bean
    public Step weeklyStep(
            ItemReader<ScoreRow> weeklyScoreReader,
            ItemProcessor<ScoreRow, AggregatedOut> weeklyProcessor,
            JdbcBatchItemWriter<AggregatedOut> weeklyUpsertWriter,
            RedisRankingWriter redisWriter
    ){
        return new StepBuilder("weeklyStep", repo)
                .<ScoreRow, AggregatedOut>chunk(1000, tx)
                .reader(weeklyScoreReader)
                .processor(weeklyProcessor)
                .writer(items -> {
                    weeklyUpsertWriter.write(items);
                    redisWriter.write(items);
                })
                .build();
    }
}
