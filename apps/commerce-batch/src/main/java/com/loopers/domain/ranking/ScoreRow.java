package com.loopers.domain.ranking;

public record ScoreRow(
        Long productId,
        long viewCnt,
        long likeCnt,
        long salesCnt,
        double score
) {}
