package com.loopers.domain.rank;

import java.time.LocalDate;

public class RankCommand {
    public record ProductRanking(
            String date,
            int size
    ){
        public static ProductRanking create(LocalDate date, int size) {
            return new ProductRanking(date.toString(), size);
        }
    }
}
