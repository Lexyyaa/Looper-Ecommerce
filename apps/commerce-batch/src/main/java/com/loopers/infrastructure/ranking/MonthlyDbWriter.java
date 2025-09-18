package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.AggregatedOut;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MonthlyDbWriter {

    private static final String UPSERT = """
        INSERT INTO product_metrics_monthly (year_month, product_id, score, updated_at)
        VALUES (:periodKey, :productId, :score, NOW())
        ON DUPLICATE KEY UPDATE score = VALUES(score), updated_at = NOW()
        """;

    @Bean
    @StepScope
    public JdbcBatchItemWriter<AggregatedOut> monthlyUpsertWriter(DataSource ds){
        var w = new JdbcBatchItemWriter<AggregatedOut>();
        w.setDataSource(ds);
        w.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        w.setSql(UPSERT);
        return w;
    }
}
