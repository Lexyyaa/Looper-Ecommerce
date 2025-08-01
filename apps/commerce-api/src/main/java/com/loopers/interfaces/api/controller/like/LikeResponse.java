package com.loopers.interfaces.api.controller.like;

import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public class LikeResponse {

    public record LikedProductList(
            long totalCount,
            List<LikedProduct> items
    ) {
        public static LikedProductList from(List<LikeInfo.LikedProduct> infos) {
            return new LikedProductList(
                    infos.size(),
                    infos.stream().map(LikedProduct::from).toList()
            );
        }
    }

    public record LikedProduct(
            Long id,
            String name,
            int price,
            long likeCount,
            Product.Status status,
            LocalDateTime createdAt
    ) {
        public static LikedProduct from(LikeInfo.LikedProduct info) {
            return new LikedProduct(
                    info.id(),
                    info.name(),
                    info.price(),
                    info.likeCount(),
                    info.status(),
                    info.createdAt()
            );
        }
    }

}
