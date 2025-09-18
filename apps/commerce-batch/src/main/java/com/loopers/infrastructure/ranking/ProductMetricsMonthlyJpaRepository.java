package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductMetricsMonthly;
import com.loopers.domain.ranking.ProductMetricsMonthlyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsMonthlyJpaRepository
        extends JpaRepository<ProductMetricsMonthly, ProductMetricsMonthlyId> {}
