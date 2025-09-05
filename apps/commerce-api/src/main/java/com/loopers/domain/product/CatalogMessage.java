package com.loopers.domain.product;

public class CatalogMessage {
    public record LikeChanged(
            Long productId,
            String targetType,
            long likeCount
    ) {}
}
