package com.loopers.domain.product;

public class ProductEvent {
    public record ProductDetailViewed(
            String loginId,
            Long productId
    ){}
}
