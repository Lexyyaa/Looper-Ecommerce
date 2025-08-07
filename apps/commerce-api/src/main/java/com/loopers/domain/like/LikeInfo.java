package com.loopers.domain.like;

import com.loopers.domain.product.Product;

import java.time.LocalDateTime;

public class LikeInfo {
    public record LikedProduct(
            Long id,
            String name,
            int price,
            long likeCount,
            Product.Status status,
            LocalDateTime createdAt
    ) {
    }

    public record Like(
            Long targetId,
            LikeTargetType targetType
    ) {
        public static LikeInfo.Like of(Long targetId, LikeTargetType targetType) {
            return new LikeInfo.Like(targetId, targetType);
        }
    }
}
