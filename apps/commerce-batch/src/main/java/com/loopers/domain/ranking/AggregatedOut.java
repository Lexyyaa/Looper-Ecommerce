package com.loopers.domain.ranking;

public record AggregatedOut(
        String periodKey,
        Long productId,
        double score,
        String redisKey,
        String redisMember
) {}
