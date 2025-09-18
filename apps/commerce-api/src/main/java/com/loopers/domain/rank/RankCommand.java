package com.loopers.domain.rank;

import java.time.LocalDate;

public class RankCommand {
    public record ProductRanking(
            String date,
            int size,
            String period
    ){
        public static ProductRanking create(String date, int size,String period) {
            return new ProductRanking(date.toString(), size,period);
        }
    }
}
