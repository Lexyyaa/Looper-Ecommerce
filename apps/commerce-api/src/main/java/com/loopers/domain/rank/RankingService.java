package com.loopers.domain.rank;


import com.loopers.config.redis.RedisConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.BASIC_ISO_DATE;
    private final @Qualifier(RedisConfig.STRING_TEMPLATE)
    StringRedisTemplate stringRedisTemplate;

    private final RankingProperties props;

    public List<Rank> getProductRank(LocalDate date, int size) {
        if (size <= 0) return List.of();

        final String key = props.keyPrefix() + ":" + date.format(dateTimeFormatter);

        Set<ZSetOperations.TypedTuple<String>> tuples =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, Math.max(0, size - 1));

        if (tuples == null || tuples.isEmpty())
            return List.of();

        List<Rank> result = new ArrayList<>(tuples.size());
        int position = 1;
        for (ZSetOperations.TypedTuple<String> t : tuples) {

            String productIdStr = t.getValue();
            Double score = t.getScore();
            if (productIdStr == null)
                continue;

            long productId = Long.parseLong(productIdStr);

            result.add(new Rank(productId, (long) position++, score == null ? 0d : score));
        }
        return result;
    }

    public Rank getRankInfo(LocalDate date, Long productId) {
        if (productId == null) return null;

        final String key = props.keyPrefix() + ":" + date.format(dateTimeFormatter);
        final String member = productId.toString();

        Long zeroBased = stringRedisTemplate.opsForZSet().reverseRank(key, member);

        if (zeroBased == null)
            return null;

        Double score = stringRedisTemplate.opsForZSet().score(key, member);

        int position = Math.toIntExact(zeroBased) + 1;

        return new Rank(productId, (long) position, score == null ? 0d : score);
    }
}
