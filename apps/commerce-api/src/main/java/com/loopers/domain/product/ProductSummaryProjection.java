package com.loopers.domain.product;

import java.time.LocalDateTime;

public interface ProductSummaryProjection {
    Long getId();
    String getName();
    Integer getMinPrice();
    Long getLikeCount();
    Product.Status getStatus();
    LocalDateTime getCreatedAt();
}
