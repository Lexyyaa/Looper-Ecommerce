package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ScoreRow;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class WeeklyAggregateReader {

    private static final String SQL = """
        SELECT pid.product_id,
               COALESCE(pm.views, 0)  AS view_cnt,
               COALESCE(pm.likes, 0)  AS like_cnt,
               COALESCE(ps.sales, 0)  AS sales_cnt
        FROM (
              SELECT product_id FROM product_metrics
               WHERE `date` BETWEEN ? AND ?
              UNION
              SELECT product_id FROM product_sku_metrics
               WHERE `date` BETWEEN ? AND ?
        ) pid
        LEFT JOIN (
              SELECT product_id,
                     SUM(view_cnt_delta) AS views,
                     SUM(like_cnt_delta) AS likes
              FROM product_metrics
              WHERE `date` BETWEEN ? AND ?
              GROUP BY product_id
        ) pm ON pm.product_id = pid.product_id
        LEFT JOIN (
              SELECT product_id,
                     SUM(sales_cnt_delta) AS sales
              FROM product_sku_metrics
              WHERE `date` BETWEEN ? AND ?
              GROUP BY product_id
        ) ps ON ps.product_id = pid.product_id
        """;

    @Bean
    @StepScope
    public JdbcCursorItemReader<ScoreRow> weeklyScoreReader(
            DataSource ds,
            @Value("#{jobParameters['start']}") String start,
            @Value("#{jobParameters['end']}")   String end
    ){
        var r = new JdbcCursorItemReader<ScoreRow>();
        r.setDataSource(ds);
        r.setSql(SQL);
        r.setRowMapper((rs, i) -> new ScoreRow(
                rs.getLong("product_id"),
                rs.getLong("view_cnt"),
                rs.getLong("like_cnt"),
                rs.getLong("sales_cnt"),
                0.0
        ));
        r.setPreparedStatementSetter(ps -> {
            ps.setString(1, start); ps.setString(2, end);
            ps.setString(3, start); ps.setString(4, end);
            ps.setString(5, start); ps.setString(6, end);
            ps.setString(7, start); ps.setString(8, end);
        });
        return r;
    }
}
