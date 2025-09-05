package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductMetricsJpaRepository extends JpaRepository<ProductMetrics, Long> {}
