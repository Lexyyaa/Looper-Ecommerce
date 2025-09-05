package com.loopers.domain.product;

public class ProductSkuMessage {
    public record StockConfirmed(
            Long productId,
            Long productSkuId,
            int amount
    ) {}
}
