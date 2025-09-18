package com.loopers.domain.ranking;

import java.util.Optional;

public interface ProductMetricsMonthlyRepository {
    Optional<ProductMetricsMonthly> findById(ProductMetricsMonthlyId id);
}
