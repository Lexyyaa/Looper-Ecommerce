package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ScoreRow;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class MonthlyFromWeeklyReader {

    private static final String SQL_TMPL = """
        SELECT product_id, SUM(score) AS score
        FROM product_metrics_weekly
        WHERE year_week IN (%s)
        GROUP BY product_id
        """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<ScoreRow> monthlyFromWeeklyReader(
            DataSource ds,
            @Value("#{jobParameters['weeksCsv']}") String weeksCsv
    ){
        String inClause = weeksCsv.replaceAll("[^0-9W,]", "").replace(",", "','");
        var r = new JdbcCursorItemReader<ScoreRow>();
        r.setDataSource(ds);
        r.setSql(SQL_TMPL.formatted("'" + inClause + "'"));
        r.setRowMapper((rs, i) -> new ScoreRow(
                rs.getLong("product_id"), 0, 0, 0, rs.getDouble("score")
        ));
        return r;
    }
}
