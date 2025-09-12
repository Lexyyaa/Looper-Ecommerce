package com.loopers.support.config.ranking;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "ranking")
public record RankingProperties(
        String keyPrefix,
        int topK,
        int dailyTtlDays,
        double carryOverAlpha,
        double viewWeight,
        double likeWeight,
        double salesWeight
) {}
