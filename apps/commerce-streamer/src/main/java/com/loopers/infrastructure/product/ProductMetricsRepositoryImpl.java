package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsId;
import com.loopers.domain.product.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {

    private final ProductMetricsJpaRepository productMetricsJpaRepository;

    private final ProductMetricsJpaRepository jpa;


    @Override public Optional<ProductMetrics> findByPkForUpdateWithLock(Long productId, LocalDate date) {
        return productMetricsJpaRepository.findByPkForUpdate(productId, date);
    }

    @Override public ProductMetrics save(ProductMetrics entity) {
        return productMetricsJpaRepository.save(entity);
    }

    @Override public List<ProductMetrics> findAllByIdProductIdInAndIdDate(Set<Long> productIds, LocalDate date) {
        return productMetricsJpaRepository.findAllByIdProductIdInAndIdDate(productIds, date);
    }
}
