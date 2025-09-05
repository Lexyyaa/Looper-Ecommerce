package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.domain.product.ProductSkuMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductSkuMetricsRepositoryImpl implements ProductSkuMetricsRepository {

    private final ProductSkuMetricsJpaRepository productSkuMetricsJpaRepository;

    @Override
    public Optional<ProductSkuMetrics> findById(long id) {
        return productSkuMetricsJpaRepository.findById(id);
    }

    @Override
    public ProductSkuMetrics save(ProductSkuMetrics productSkuMetrics) {
        return productSkuMetricsJpaRepository.save(productSkuMetrics);
    }

    @Override
    public void upsertAddSales(Long productId, Long skuId, Long delta, Instant now) {
        productSkuMetricsJpaRepository.upsertAddSales(productId, skuId, delta, now);
    }
}
