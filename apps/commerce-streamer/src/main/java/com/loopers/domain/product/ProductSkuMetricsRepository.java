package com.loopers.domain.product;

import com.loopers.infrastructure.product.SalesSum;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface ProductSkuMetricsRepository {
    ProductSkuMetrics save(ProductSkuMetrics productSkuMetrics);
    void upsertAddSales(Long productId, Long skuId, Long delta, Instant now);
    List<SalesSum> sumSalesByProductIds(Collection<Long> ids);
}
