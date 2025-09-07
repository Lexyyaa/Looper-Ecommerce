package com.loopers.domain.product;

import java.time.Instant;

public interface ProductSkuMetricsRepository {
    ProductSkuMetrics save(ProductSkuMetrics productSkuMetrics);
    void upsertAddSales(Long productId, Long skuId, Long delta, Instant now);
}
