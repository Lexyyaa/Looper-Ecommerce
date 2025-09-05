package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import com.loopers.domain.product.ProductMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductMetricsRepositoryImpl implements ProductMetricsRepository {
    private final ProductMetricsJpaRepository productMetricsJpaRepository;


    @Override
    public ProductMetrics save(ProductMetrics productMetrics) {
        return productMetricsJpaRepository.save(productMetrics);
    }

    @Override
    public Optional<ProductMetrics> findById(long id) {
        return productMetricsJpaRepository.findById(id);
    }
}
