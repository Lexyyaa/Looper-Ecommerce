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

    private static final DateTimeFormatter YMD = DateTimeFormatter.BASIC_ISO_DATE;

    private final @Qualifier(RedisConfig.STRING_TEMPLATE_MASTER)
    StringRedisTemplate stringRedisTemplate;

    private final RankingProperties props;
    private final MetricsService metricsService;

    public void computeAndPutScores(LocalDate date, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) return;

        String key = props.keyPrefix() + ":" + date.format(YMD);
        BoundZSetOperations<String, String> zops = stringRedisTemplate.boundZSetOps(key);

        Map<Long, ProductMetrics> pmById = metricsService.getProductMetrics(productIds, date);

        Map<Long, Long> salesByProductIds = metricsService.getSalesSumByProductIds(productIds, date);

        double vw = props.viewWeight();
        double lw = props.likeWeight();
        double sw = props.salesWeight();

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>(productIds.size());
        for (Long id : productIds) {
            ProductMetrics pm = pmById.get(id);
            long viewDelta = (pm == null ? 0L : pm.getViewCntDelta());
            long likeDelta = (pm == null ? 0L : pm.getLikeCntDelta());
            long salesDelta = salesByProductIds.getOrDefault(id, 0L);

            double score = viewDelta * vw + likeDelta * lw + salesDelta * sw;
            tuples.add(new DefaultTypedTuple<>(String.valueOf(id), score));
        }

        if (tuples.isEmpty()) return;

        zops.add(tuples);
        zops.expire(Duration.ofDays(props.dailyTtlDays()));
    }
}

