package com.loopers.support.config.ranking;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ranking")
public record RankingProperties(
        String weeklyPrefix,
        String monthlyPrefix,
        int topK,
        int weeklyTtlDays,
        int monthlyTtlDays,
        Weights weight,
        double carryOverAlpha
) {
    public record Weights(double view, double like, double sales) {}
}
