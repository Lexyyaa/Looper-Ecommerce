package com.loopers.domain.rank;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "ranking")
public record RankingProperties(
        String keyPrefix,
        String weeklyPrefix,
        String monthlyPrefix
){}
