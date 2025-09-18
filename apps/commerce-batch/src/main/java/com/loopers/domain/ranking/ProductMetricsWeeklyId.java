package com.loopers.domain.ranking;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public record ProductMetricsWeeklyId(
    @Column(name="year_week", length=7, nullable=false)
    String yearWeek,
    @Column(name="product_id", nullable=false)
    Long productId
) implements Serializable {}
