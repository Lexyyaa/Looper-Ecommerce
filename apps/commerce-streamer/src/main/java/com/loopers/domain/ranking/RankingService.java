package com.loopers.domain.ranking;

import com.loopers.config.redis.RedisConfig;
import com.loopers.domain.product.MetricsService;
import com.loopers.domain.product.ProductMetrics;
import com.loopers.support.config.ranking.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
@RequiredArgsConstructor
public class RankingService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE;

    private final @Qualifier(RedisConfig.STRING_TEMPLATE_MASTER)
    StringRedisTemplate stringRedisTemplate;

    private final RankingProperties props;
    private final MetricsService metricsService;

    public void computeAndPutScoresToday(Set<Long> productIds) {
        computeAndPutScores(LocalDate.now(ZoneId.of("Asia/Seoul")), productIds);
    }

    public void computeAndPutScores(LocalDate date, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        String key = props.keyPrefix() + ":" + date.format(dateTimeFormatter);
        BoundZSetOperations<String, String> zops = stringRedisTemplate.boundZSetOps(key);

        Map<Long, ProductMetrics> productMetrics = metricsService.getProductMetrics(productIds);
        Map<Long, Long> salesByProductIds = metricsService.getSalesSumByProductIds(productIds);

        double vw = props.viewWeight();
        double lw = props.likeWeight();
        double sw = props.salesWeight();

        Set<ZSetOperations.TypedTuple<String>>  tuples = new LinkedHashSet<>(productIds.size());
        for (Long id : productIds) {
            long viewCnt = productMetrics.get(id).getViewCnt();
            long likeCnt = productMetrics.get(id).getLikeCnt();
            long salesSum = salesByProductIds.getOrDefault(id, 0L);

            double score = viewCnt * vw + likeCnt * lw + salesSum * sw;

            tuples.add(new DefaultTypedTuple<>(String.valueOf(id), score));
        }

        if (tuples.isEmpty())
            return;

        zops.add(tuples);
        zops.expire(Duration.ofDays(props.dailyTtlDays()));
    }
}
