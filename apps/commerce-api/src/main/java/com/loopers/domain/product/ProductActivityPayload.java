package com.loopers.domain.product;

public class ProductActivityPayload {
    public record ProductDetailViewed(
            String loginId,
            Long productId
    ) {}
}
