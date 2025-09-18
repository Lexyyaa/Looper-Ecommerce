package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductMetricsMonthly;
import com.loopers.domain.ranking.ProductMetricsMonthlyId;
import com.loopers.domain.ranking.ProductMetricsMonthlyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductMetricsMonthlyRepositoryImpl implements ProductMetricsMonthlyRepository {

    private final ProductMetricsMonthlyJpaRepository productMetricsMonthlyJpaRepository;

    @Override
    public Optional<ProductMetricsMonthly> findById(ProductMetricsMonthlyId id) {
        return productMetricsMonthlyJpaRepository.findById(id);
    }
}
