package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductMetricsWeekly;
import com.loopers.domain.ranking.ProductMetricsWeeklyId;
import com.loopers.domain.ranking.ProductMetricsWeeklyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductMetricsWeeklyRepositoryImpl implements ProductMetricsWeeklyRepository {

    private final ProductMetricsWeeklyJpaRepository productMetricsWeeklyJpaRepository;

    @Override
    public Optional<ProductMetricsWeekly> findById(ProductMetricsWeeklyId id) {
        return productMetricsWeeklyJpaRepository.findById(id);
    }
}
