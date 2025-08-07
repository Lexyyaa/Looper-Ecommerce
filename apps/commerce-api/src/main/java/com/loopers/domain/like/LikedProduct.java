package com.loopers.domain.like;

import com.loopers.domain.product.Product;

import java.time.LocalDateTime;

public record LikedProduct(
        Long id,
        String name,
        int price,
        long likeCount,
        Product.Status status,
        LocalDateTime createdAt
) {
    public static LikedProduct of(Long id, String name, int price, long likeCount, Product.Status status, LocalDateTime createdAt) {
        return new LikedProduct(id, name, price, likeCount, status, createdAt);
    }
}
