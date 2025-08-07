package com.loopers.interfaces.api.controller.like;

import com.loopers.domain.like.LikeInfo;
import com.loopers.domain.like.LikeTargetType;
import com.loopers.domain.product.Product;

import java.time.LocalDateTime;
import java.util.List;

public class LikeV1Response {

    public record Like(
            Long targetId,
            LikeTargetType targetType
    ) {
        public static LikeV1Response.Like from(LikeInfo.Like like) {
            return new LikeV1Response.Like(like.targetId(), like.targetType());
        }
    }

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
