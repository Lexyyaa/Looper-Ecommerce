package com.loopers.domain.product;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductMetricsRepository {

    Optional<ProductMetrics> findByPkForUpdateWithLock(Long productId, LocalDate date);

    ProductMetrics save(ProductMetrics entity);

    List<ProductMetrics> findAllByIdProductIdInAndIdDate(Set<Long> productIds, LocalDate date);
}
