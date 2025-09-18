package com.loopers.domain.ranking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record ProductMetricsMonthlyId(
        @Column(name="year_month", length=7, nullable=false)
        String yearMonth,
        @Column(name="product_id", nullable=false)
        Long productId
) implements Serializable {}
