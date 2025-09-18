package com.loopers.domain.product;

import com.loopers.infrastructure.product.ProductSkuMetricsJpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductSkuMetricsRepository {

    Optional<ProductSkuMetrics> findById(ProductSkuMetricsId id);

    Optional<ProductSkuMetrics> findByPkForUpdateWithLock(Long productSkuId, LocalDate date);

    ProductSkuMetrics save(ProductSkuMetrics entity);

    List<ProductSkuMetricsJpaRepository.SalesSum> sumSalesByProductIdsAndDate(Set<Long> productIds,LocalDate date);
}
