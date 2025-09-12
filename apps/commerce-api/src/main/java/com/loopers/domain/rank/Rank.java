package com.loopers.domain.rank;

import lombok.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Getter
@ToString
@EqualsAndHashCode(of = "productId")
@AllArgsConstructor
@Builder
public class Rank {
    private final Long productId;
    private final int position;
    private final double score;

    public static Rank create(Long productId, int position, double score) {
        return new Rank(productId, position, score);
    }
}
