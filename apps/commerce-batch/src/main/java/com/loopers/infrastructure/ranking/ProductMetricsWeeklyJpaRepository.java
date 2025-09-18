package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.ProductMetricsWeekly;
import com.loopers.domain.ranking.ProductMetricsWeeklyId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsWeeklyJpaRepository
        extends JpaRepository<ProductMetricsWeekly, ProductMetricsWeeklyId> {}
