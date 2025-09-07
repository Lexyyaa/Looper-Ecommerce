package com.loopers.domain.product;

import java.util.Optional;

public interface ProductMetricsRepository {
    ProductMetrics save(ProductMetrics productMetrics);
    Optional<ProductMetrics> findById(long id);
}
