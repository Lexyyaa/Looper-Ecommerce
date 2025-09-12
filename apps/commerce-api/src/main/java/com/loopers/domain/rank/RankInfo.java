package com.loopers.domain.rank;

import com.loopers.domain.product.Product;

public class RankInfo {
    public record ProductRank(
        int rank,
        int score,
        Long productId,
        String name,
        Long price
    ){
        public static ProductRank from(Rank rank, Product product) {
            int score = (int) Math.round(rank.getScore());
            return new ProductRank(
                    rank.getPosition(),
                    score,
                    rank.getProductId(),
                    product.getName(),
                    product.getMinPrice());
        }
    }
}
