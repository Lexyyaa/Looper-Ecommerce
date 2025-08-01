package com.loopers.domain.product;

import java.time.LocalDateTime;

public record ProductSummary(
        Long id,
        String name,
        int price,
        long likeCount,
        Product.Status status,
        LocalDateTime createdAt
) {
    public static ProductSummary from(Product product, int minPrice, long likeCount, Product.Status status,LocalDateTime createdAt) {
        return new ProductSummary(
                product.getId(),
                product.getName(),
                minPrice,
                likeCount,
                status,
                createdAt
        );
    }
}
