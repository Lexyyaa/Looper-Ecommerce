package com.loopers.domain.rank;

import com.loopers.domain.product.Product;

public class RankInfo {
    public record ProductRank(
        Long rank,
        Double score,
        Long productId,
        String name,
        Long price
    ){
        public static ProductRank from(Rank rank, Product product) {
            return new ProductRank(
                    rank.getPosition(),
                    rank.getScore(),
                    rank.getProductId(),
                    product.getName(),
                    product.getMinPrice());
        }
    }
}
