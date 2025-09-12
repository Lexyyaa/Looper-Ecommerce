package com.loopers.interfaces.api.controller.rank;


import com.loopers.domain.rank.RankInfo;

import java.util.List;

public class RankV1Response {
    public record ProductRankList(
            long totalCount,
            List<ProductRank> productRankList
    ){
        public static ProductRankList from(List<RankInfo.ProductRank> infos){
            List<ProductRank> productRankList = infos.stream()
                    .map(ProductRank::from)
                    .toList();
            return new ProductRankList(infos.size(), productRankList);
        }
    }

    public record ProductRank(
            int rank,
            int score,
            Long productId,
            String name,
            Long price
    ){
        public static ProductRank from(RankInfo.ProductRank info){
            return new ProductRank(
                    info.rank(),
                    info.score(),
                    info.productId(),
                    info.name(),
                    info.price()
            );
        }
    }
}
