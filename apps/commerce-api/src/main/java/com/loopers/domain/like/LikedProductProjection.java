package com.loopers.domain.like;

import com.loopers.domain.product.Product;

import java.time.LocalDateTime;

public interface LikedProductProjection {
    Long getId();
    String getName();
    Integer getMinPrice();
    Long getLikeCount();
    Product.Status getStatus();
    LocalDateTime getCreatedAt();
}
