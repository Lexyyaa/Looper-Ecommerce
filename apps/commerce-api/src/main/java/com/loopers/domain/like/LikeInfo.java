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
//        public static LikeInfo.LikedProduct from(LikedProduct product) {
//            return new LikeInfo.LikedProduct(
//                    product.id(),
//                    product.name(),
//                    product.price(),
//                    product.likeCount(),
//                    product.status(),
//                    product.createdAt()
//            );
//        }
    }
}
