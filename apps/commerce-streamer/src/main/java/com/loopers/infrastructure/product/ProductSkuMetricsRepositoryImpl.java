package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductSkuMetrics;
import com.loopers.domain.product.ProductSkuMetricsId;
import com.loopers.domain.product.ProductSkuMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductSkuMetricsRepositoryImpl implements ProductSkuMetricsRepository {

    private final ProductSkuMetricsJpaRepository productSkuMetricsJpaRepository;


    private final ProductSkuMetricsJpaRepository jpa;

    @Override public Optional<ProductSkuMetrics> findById(ProductSkuMetricsId id) {
        return productSkuMetricsJpaRepository.findById(id);
    }

    @Override public Optional<ProductSkuMetrics> findByPkForUpdateWithLock(Long productSkuId, LocalDate date) {
        return productSkuMetricsJpaRepository.findByPkForUpdate(productSkuId, date);
    }

    @Override public ProductSkuMetrics save(ProductSkuMetrics entity) {
        return productSkuMetricsJpaRepository.save(entity);
    }

    @Override
    public List<ProductSkuMetricsJpaRepository.SalesSum> sumSalesByProductIdsAndDate(Set<Long> productIds, LocalDate date) {
        return productSkuMetricsJpaRepository.sumSalesByProductIdsAndDate(productIds,date);
    }
}
