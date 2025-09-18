package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.AggregatedOut;
import com.loopers.support.config.ranking.RankingProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisRankingWriter implements ItemWriter<AggregatedOut> {

    private final StringRedisTemplate redis;
    private final RankingProperties props;


    @Override
    public void write(Chunk<? extends AggregatedOut> chunk) throws Exception {
        if (chunk == null || chunk.isEmpty()) return;

        String key = chunk.getItems().get(0).redisKey();
        BoundZSetOperations<String, String> zops = redis.boundZSetOps(key);

        Set<ZSetOperations.TypedTuple<String>> tuples = new LinkedHashSet<>(chunk.size());
        for (AggregatedOut it : chunk) {
            tuples.add(new DefaultTypedTuple<>(it.redisMember(), it.score()));
        }

        if (tuples.isEmpty()) return;

        zops.add(tuples);

        zops.removeRange(0, -(props.topK() + 1));

        Long ttl = redis.getExpire(key);
        if (ttl == null || ttl < 0) {
            int days = key.startsWith(props.weeklyPrefix() + ":")
                    ? props.weeklyTtlDays()
                    : props.monthlyTtlDays();
            zops.expire(Duration.ofDays(days));
        }
    }
}
