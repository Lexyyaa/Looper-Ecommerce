package com.loopers.domain.ranking;

import java.util.Optional;

public interface ProductMetricsWeeklyRepository {
    Optional<ProductMetricsWeekly> findById(ProductMetricsWeeklyId id);
}
